package com.in28minutes.webservices.songrec.config;

import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import com.in28minutes.webservices.songrec.service.openai.EmbeddingService;
import com.in28minutes.webservices.songrec.service.qdrant.TrackSemanticSearchService;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("bench")
public class NPlus1BenchmarkRunner implements CommandLineRunner {

  private final TrackSemanticSearchService trackSemanticSearchService;
  private final EmbeddingService embeddingService;
  private final EntityManagerFactory entityManagerFactory;

  @Override
  public void run(String... args) {
    Long userId = 15L; // 실제 존재하는 테스트 유저 id로 교체

    List<Float> queryVector = embeddingService.embedText("비 오는 날 혼자 듣기 좋은 노래");
    List<Point> candidates = trackSemanticSearchService.searchCandidates(queryVector, 50);
    log.info("candidateCount={}", candidates == null ? 0 : candidates.size());
    Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    stats.setStatisticsEnabled(true);

    int runs = 10;
    long totalMs = 0;

    for (int i = 0; i < runs; i++) {
      stats.clear();
      long start = System.nanoTime();
      trackSemanticSearchService.selectRerankedCandidates(queryVector, candidates, userId);
      long elapsedMs = (System.nanoTime() - start) / 1_000_000;

      if (i > 0) {
        totalMs += elapsedMs; // 첫 실행(워밍업)은 평균에서 제외
      }
      log.info("run={}, elapsedMs={}, queryCount={}", i, elapsedMs, stats.getQueryExecutionCount());
    }

    log.info("avgElapsedMs(excludingWarmup)={}", totalMs / (runs - 1));
  }
}