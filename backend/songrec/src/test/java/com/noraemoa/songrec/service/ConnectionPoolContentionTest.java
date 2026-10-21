package com.noraemoa.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.noraemoa.songrec.config.QdrantInitializer;
import com.noraemoa.songrec.config.perf.ConnectionUsageTracker;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserRole;
import com.noraemoa.songrec.dto.request.RequestCreateRequestDto;
import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.noraemoa.songrec.integration.qdrant.client.QdrantClient;
import com.noraemoa.songrec.repository.TrackRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.service.fileStorage.FileStorageService;
import com.noraemoa.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import com.noraemoa.songrec.service.openai.EmbeddingService;
import com.noraemoa.songrec.service.openai.TrackSearchQueryAnalysisService;
import com.noraemoa.songrec.support.ExternalApiStubs;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

/**
 * 커넥션 풀 고갈 재현 테스트.
 *
 * 커넥션 풀 2개, OpenAI 프롬프트 정제 응답 2초, 동시 요청 5개.
 * - 외부 호출이 트랜잭션 안에 있으면(수정 전): 요청마다 커넥션을 2초 이상 붙잡으므로
 *   3번째 요청부터 커넥션을 기다리고, connection-timeout(3초)을 넘기면 실패한다.
 * - 외부 호출이 트랜잭션 밖에 있으면(수정 후): 커넥션은 DB 작업 동안만 쓰이므로 대기가 거의 없다.
 *
 * 같은 테스트를 수정 전 코드(baseline worktree)에서도 돌려 CONTENTION_RESULT 로그를 비교했다.
 */
@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:songrec_contention;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
    "spring.datasource.hikari.maximum-pool-size=2",
    "spring.datasource.hikari.minimum-idle=2",
    "spring.datasource.hikari.connection-timeout=3000"
})
@ActiveProfiles("test")
class ConnectionPoolContentionTest {

  private static final Logger log = LoggerFactory.getLogger(ConnectionPoolContentionTest.class);
  private static final int CONCURRENT_REQUESTS = 5;
  private static final long OPENAI_DELAY_MS = 2000;

  @MockBean
  private QdrantInitializer qdrantInitializer;
  @MockBean
  private RequestPromptAiService requestPromptAiService;
  @MockBean
  private RequestThumbnailAiService requestThumbnailAiService;
  @MockBean
  private FileStorageService fileStorageService;
  @MockBean
  private TrackSearchQueryAnalysisService trackSearchQueryAnalysisService;
  @MockBean
  private EmbeddingService embeddingService;
  @MockBean
  private QdrantClient qdrantClient;

  @Autowired
  private RequestService requestService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private TrackRepository trackRepository;

  record Result(int index, boolean success, boolean connectionTimeout, long waitMs, long holdMs,
                long elapsedMs, String error) {
  }

  @Test
  @DisplayName("풀 2개 + OpenAI 2초 + 동시 요청 5개에서도 커넥션 대기/타임아웃이 생기지 않는다")
  void noConnectionWaitUnderConcurrentRequests() throws Exception {
    String unique = UUID.randomUUID().toString().replace("-", "");
    Long userId = userRepository.save(User.builder()
        .email(unique + "@test.local").username("c" + unique.substring(0, 8))
        .password("pw").role(UserRole.USER).build()).getId();
    List<Long> trackIds = List.of(
        trackRepository.save(Track.builder().spotifyId("a" + unique).name("A").build()).getId(),
        trackRepository.save(Track.builder().spotifyId("b" + unique).name("B").build()).getId());

    // OpenAI 응답이 2초 걸리는 상황. 키워드는 요청마다 달라서 unique 제약 충돌이 나지 않게 한다.
    when(requestPromptAiService.refine(anyString())).thenAnswer(inv -> {
      Thread.sleep(OPENAI_DELAY_MS);
      String prompt = inv.getArgument(0);
      return new RequestPromptRefineResult("동시 요청",
          List.of("kw" + unique + Math.abs(prompt.hashCode())));
    });
    ExternalApiStubs.stubSearch(trackSearchQueryAnalysisService, embeddingService, qdrantClient,
        trackIds);
    when(requestThumbnailAiService.generateThumbnail(anyString(), anyString(), anyList()))
        .thenReturn(new byte[]{1});
    when(fileStorageService.storeGeneratedThumbnail(anyLong(), any()))
        .thenReturn(new StoredFile("k", "/uploads/k"));

    // 워밍업 1회: 첫 호출의 Hibernate 쿼리 준비, JIT 같은 콜드 스타트 비용이 측정에 섞이지 않게 한다.
    requestService.createRequest(new RequestCreateRequestDto("워밍업"), userId);

    ExecutorService pool = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
    CountDownLatch startSignal = new CountDownLatch(1);
    List<Future<Result>> futures = new ArrayList<>();
    for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
      int index = i + 1;
      futures.add(pool.submit(() -> {
        startSignal.await();
        ConnectionUsageTracker.start();
        long start = System.nanoTime();
        boolean success = false;
        boolean timeout = false;
        String error = null;
        try {
          requestService.createRequest(new RequestCreateRequestDto("동시 요청 " + index), userId);
          success = true;
        } catch (Exception e) {
          timeout = hasCause(e, SQLTransientConnectionException.class);
          error = e.getClass().getSimpleName();
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        ConnectionUsageTracker.Stats stats = ConnectionUsageTracker.stop();
        return new Result(index, success, timeout, stats.waitMaxMs(), stats.holdTotalMs(),
            elapsedMs, error);
      }));
    }
    long batchStart = System.nanoTime();
    startSignal.countDown();
    List<Result> results = new ArrayList<>();
    for (Future<Result> future : futures) {
      results.add(future.get());
    }
    long batchMs = (System.nanoTime() - batchStart) / 1_000_000;
    pool.shutdown();

    results.forEach(r -> log.info(
        "CONTENTION_REQUEST index={} success={} connectionTimeout={} connWaitMaxMs={} connHoldTotalMs={} elapsedMs={} error={}",
        r.index(), r.success(), r.connectionTimeout(), r.waitMs(), r.holdMs(), r.elapsedMs(),
        r.error()));
    long successCount = results.stream().filter(Result::success).count();
    long timeoutCount = results.stream().filter(Result::connectionTimeout).count();
    long maxWaitMs = results.stream().mapToLong(Result::waitMs).max().orElse(0);
    long totalWaitMs = results.stream().mapToLong(Result::waitMs).sum();
    log.info(
        "CONTENTION_RESULT requests={} success={} failures={} connectionTimeouts={} maxConnWaitMs={} totalConnWaitMs={} batchMs={}",
        CONCURRENT_REQUESTS, successCount, CONCURRENT_REQUESTS - successCount, timeoutCount,
        maxWaitMs, totalWaitMs, batchMs);

    assertThat(successCount).isEqualTo(CONCURRENT_REQUESTS);
    assertThat(timeoutCount).isZero();
    // 커넥션을 DB 작업 동안만 쓰므로 대기가 OpenAI 지연(2초)보다 훨씬 짧아야 한다.
    // (수정 전에는 앞 요청이 OpenAI 를 기다리는 동안 커넥션을 쥐고 있어 2초 이상 기다린다)
    assertThat(maxWaitMs).isLessThan(OPENAI_DELAY_MS / 2);
  }

  private static boolean hasCause(Throwable e, Class<? extends Throwable> type) {
    for (Throwable t = e; t != null; t = t.getCause()) {
      if (type.isInstance(t)) {
        return true;
      }
    }
    return false;
  }
}
