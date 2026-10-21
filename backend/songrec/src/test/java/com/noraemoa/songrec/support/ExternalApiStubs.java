package com.noraemoa.songrec.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.noraemoa.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import com.noraemoa.songrec.integration.qdrant.client.QdrantClient;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse.QdrantResultResponse;
import com.noraemoa.songrec.service.openai.EmbeddingService;
import com.noraemoa.songrec.service.openai.TrackSearchQueryAnalysisService;
import java.util.ArrayList;
import java.util.List;

/**
 * 트랙 검색에 쓰이는 외부 API(OpenAI 질의 분석/임베딩, Qdrant)를 모킹하는 공통 스텁.
 * TrackSemanticSearchService 의 재정렬 로직과 DB 조회는 실제로 실행된다.
 */
public final class ExternalApiStubs {

  public static final List<Float> QUERY_VECTOR = List.of(1f, 0f, 0f);

  private ExternalApiStubs() {
  }

  public static void stubSearch(TrackSearchQueryAnalysisService analysisService,
      EmbeddingService embeddingService, QdrantClient qdrantClient, List<Long> trackIds) {
    when(analysisService.analyze(anyString())).thenReturn(TrackSearchQueryAnalysisResult.builder()
        .mood_tags(List.of("calm")).scene_tags(List.of("rain")).short_description("test").build());
    when(embeddingService.embedText(anyString())).thenReturn(QUERY_VECTOR);

    when(qdrantClient.searchSong(any(), anyInt())).thenReturn(songResponse(trackIds));
    // 과거 유사 질의: DB 에 없는 requestId 하나만 돌려준다(피드백 점수 0).
    when(qdrantClient.searchQuery(any(), anyInt()))
        .thenReturn(response(List.of(point(999_999L, 0.5, null))));
  }

  // 후보곡끼리 코사인 유사도가 0.95 를 넘으면 걸러지므로 서로 직교하는 벡터를 준다.
  public static QdrantSearchResponse songResponse(List<Long> trackIds) {
    List<Point> songPoints = new ArrayList<>();
    for (int i = 0; i < trackIds.size(); i++) {
      List<Float> vector = new ArrayList<>(List.of(0f, 0f, 0f));
      vector.set(i % 3, 1f);
      songPoints.add(point(trackIds.get(i), 0.9 - i * 0.1, vector));
    }
    return response(songPoints);
  }

  public static Point point(Long id, double score, List<Float> vector) {
    Point point = new Point();
    point.setId(id);
    point.setScore(score);
    point.setVector(vector);
    return point;
  }

  public static QdrantSearchResponse response(List<Point> points) {
    QdrantResultResponse result = new QdrantResultResponse();
    result.setPoints(points);
    QdrantSearchResponse response = new QdrantSearchResponse();
    response.setResult(result);
    return response;
  }
}
