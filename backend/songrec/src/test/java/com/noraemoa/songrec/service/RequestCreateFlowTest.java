package com.noraemoa.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.noraemoa.songrec.config.QdrantInitializer;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserRole;
import com.noraemoa.songrec.dto.request.RequestCreateRequestDto;
import com.noraemoa.songrec.dto.response.request.RequestResponseDto;
import com.noraemoa.songrec.global.exception.ExternalApiException;
import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.noraemoa.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import com.noraemoa.songrec.integration.qdrant.client.QdrantClient;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantPoint;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.noraemoa.songrec.repository.KeywordRepository;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.RequestTrackRepository;
import com.noraemoa.songrec.repository.TrackRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.service.fileStorage.FileStorageService;
import com.noraemoa.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import com.noraemoa.songrec.service.openai.EmbeddingService;
import com.noraemoa.songrec.service.openai.TrackSearchQueryAnalysisService;
import com.noraemoa.songrec.support.ExternalApiStubs;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 추천 요청 생성 흐름 통합 테스트(H2).
 * 외부 API(OpenAI, Qdrant, 파일 저장소)만 모킹하고, 트랜잭션/이벤트/비동기는 실제 스프링 설정으로 실행한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class RequestCreateFlowTest {

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
  @Autowired
  private RequestRepository requestRepository;
  @Autowired
  private RequestTrackRepository requestTrackRepository;
  @Autowired
  private KeywordRepository keywordRepository;

  private Long userId;
  private List<Long> trackIds;
  private String keyword;

  @BeforeEach
  void setUp() throws Exception {
    String unique = UUID.randomUUID().toString().replace("-", "");
    userId = userRepository.save(User.builder()
        .email(unique + "@test.local").username("u" + unique.substring(0, 8))
        .password("pw").role(UserRole.USER).build()).getId();
    trackIds = List.of(
        trackRepository.save(Track.builder().spotifyId("a" + unique).name("A").build()).getId(),
        trackRepository.save(Track.builder().spotifyId("b" + unique).name("B").build()).getId());
    keyword = "kw" + unique;

    when(requestPromptAiService.refine(anyString()))
        .thenReturn(new RequestPromptRefineResult("비 오는 날 티타임", List.of(keyword)));
    ExternalApiStubs.stubSearch(trackSearchQueryAnalysisService, embeddingService, qdrantClient,
        trackIds);
    when(requestThumbnailAiService.generateThumbnail(anyString(), anyString(), anyList()))
        .thenReturn(new byte[]{1, 2, 3});
    when(fileStorageService.storeGeneratedThumbnail(anyLong(), any()))
        .thenAnswer(inv -> new StoredFile("requests/" + inv.getArgument(0) + "_thumb.png",
            "/uploads/requests/" + inv.getArgument(0) + "_thumb.png"));
  }

  @Test
  @DisplayName("외부 API 호출은 모두 트랜잭션 밖에서 일어나고, Qdrant 질의 벡터 저장은 Request 커밋 이후에 일어난다")
  void externalCallsRunOutsideTransaction() {
    Map<String, Boolean> txActiveInCall = new ConcurrentHashMap<>();
    Map<String, Boolean> requestCommittedAtUpsert = new ConcurrentHashMap<>();
    Map<String, String> thumbnailThread = new ConcurrentHashMap<>();

    when(requestPromptAiService.refine(anyString())).thenAnswer(inv -> {
      txActiveInCall.put("refine", TransactionSynchronizationManager.isActualTransactionActive());
      return new RequestPromptRefineResult("비 오는 날 티타임", List.of(keyword));
    });
    when(trackSearchQueryAnalysisService.analyze(anyString())).thenAnswer(inv -> {
      txActiveInCall.put("analyze", TransactionSynchronizationManager.isActualTransactionActive());
      return TrackSearchQueryAnalysisResult.builder()
          .mood_tags(List.of("calm")).build();
    });
    when(embeddingService.embedText(anyString())).thenAnswer(inv -> {
      txActiveInCall.put("embed", TransactionSynchronizationManager.isActualTransactionActive());
      return ExternalApiStubs.QUERY_VECTOR;
    });
    QdrantSearchResponse songResponse = ExternalApiStubs.songResponse(trackIds);
    when(qdrantClient.searchSong(any(), anyInt())).thenAnswer(inv -> {
      txActiveInCall.put("qdrantSearch",
          TransactionSynchronizationManager.isActualTransactionActive());
      return songResponse;
    });
    when(qdrantClient.upsertQueryEmbeddingPoint(any())).thenAnswer(inv -> {
      txActiveInCall.put("qdrantUpsert",
          TransactionSynchronizationManager.isActualTransactionActive());
      Long requestId = ((Number) ((QdrantPoint) inv.getArgument(0)).getId()).longValue();
      // 트랜잭션 없이 새 커넥션으로 조회한다. 보이면 이미 커밋됐다는 뜻이다.
      requestCommittedAtUpsert.put("committed", requestRepository.existsById(requestId));
      return "ok";
    });
    when(requestThumbnailAiService.generateThumbnail(anyString(), anyString(), anyList()))
        .thenAnswer(inv -> {
          txActiveInCall.put("thumbnail",
              TransactionSynchronizationManager.isActualTransactionActive());
          thumbnailThread.put("name", Thread.currentThread().getName());
          return new byte[]{1, 2, 3};
        });

    RequestResponseDto response = requestService.createRequest(dto("비 오는 날 듣기 좋은 노래"), userId);

    verify(requestThumbnailAiService, timeout(5000)).generateThumbnail(anyString(), anyString(),
        anyList());
    assertThat(txActiveInCall).containsOnlyKeys("refine", "analyze", "embed", "qdrantSearch",
        "qdrantUpsert", "thumbnail");
    assertThat(txActiveInCall.values()).allMatch(active -> !active);
    assertThat(requestCommittedAtUpsert.get("committed")).isTrue();
    assertThat(thumbnailThread.get("name")).startsWith("thumbnail-");

    // 응답 시점에는 썸네일이 없고, 비동기 작업이 끝나면 DB 에 반영된다.
    assertThat(response.getThumbnailUrl()).isNull();
    assertThat(awaitThumbnailUrl(response.getId()))
        .isEqualTo("/uploads/requests/" + response.getId() + "_thumb.png");
  }

  @Test
  @DisplayName("썸네일 생성이 실패해도 추천 요청 생성은 성공하고 추천곡도 저장된다")
  void thumbnailFailureDoesNotAffectRequest() throws Exception {
    when(requestThumbnailAiService.generateThumbnail(anyString(), anyString(), anyList()))
        .thenThrow(new RuntimeException("OpenAI image API down"));

    RequestResponseDto response = requestService.createRequest(dto("비 오는 날 듣기 좋은 노래"), userId);

    verify(requestThumbnailAiService, timeout(5000)).generateThumbnail(anyString(), anyString(),
        anyList());
    verify(fileStorageService, after(300).never()).storeGeneratedThumbnail(anyLong(), any());

    Request saved = requestRepository.findById(response.getId()).orElseThrow();
    assertThat(saved.getThumbnailUrl()).isNull();
    assertThat(trackIds).allMatch(trackId ->
        requestTrackRepository.findByRequest_IdAndTrack_Id(response.getId(), trackId).isPresent());
  }

  @Test
  @DisplayName("프롬프트 정제가 실패하면 프롬프트 앞 30자를 fallback 제목으로 저장한다")
  void refineFailureFallsBackToPromptTitle() {
    when(requestPromptAiService.refine(anyString()))
        .thenThrow(new RuntimeException("OpenAI chat API timeout"));
    String prompt = "비 오는 날 혼자 차 마시면서 듣기 좋은 노래를 추천해 주세요 잔잔한 걸로요";

    RequestResponseDto response = requestService.createRequest(dto(prompt), userId);

    Request saved = requestRepository.findById(response.getId()).orElseThrow();
    assertThat(saved.getTitle()).isEqualTo(prompt.substring(0, 30));
    assertThat(response.getKeywords()).isEmpty();
  }

  @Test
  @DisplayName("트랙 검색(외부 API)이 실패하면 503 예외를 던지고 Request/Keyword 를 하나도 저장하지 않는다")
  void searchFailureLeavesNoPartialData() throws Exception {
    when(trackSearchQueryAnalysisService.analyze(anyString()))
        .thenThrow(new RuntimeException("OpenAI chat API down"));

    assertThatThrownBy(() -> requestService.createRequest(dto("비 오는 날 듣기 좋은 노래"), userId))
        .isInstanceOf(ExternalApiException.class);

    assertThat(requestRepository.findAllByUserIdAndDeletedFalse(userId)).isEmpty();
    assertThat(keywordRepository.findByNormalizedText(keyword)).isEmpty();
    verify(qdrantClient, never()).upsertQueryEmbeddingPoint(any());
    verify(requestThumbnailAiService, after(300).never())
        .generateThumbnail(anyString(), anyString(), anyList());
  }

  private RequestCreateRequestDto dto(String prompt) {
    return new RequestCreateRequestDto(prompt);
  }

  private String awaitThumbnailUrl(Long requestId) {
    long deadline = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < deadline) {
      String url = requestRepository.findById(requestId).orElseThrow().getThumbnailUrl();
      if (url != null) {
        return url;
      }
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    return null;
  }
}
