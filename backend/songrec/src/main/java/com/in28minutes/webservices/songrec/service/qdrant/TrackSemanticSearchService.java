package com.in28minutes.webservices.songrec.service.qdrant;

import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackSemanticSearchItemDto;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import com.in28minutes.webservices.songrec.integration.qdrant.client.QdrantClient;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import com.in28minutes.webservices.songrec.service.openai.EmbeddingService;
import com.in28minutes.webservices.songrec.service.openai.TrackSearchQueryAnalysisService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackSemanticSearchService {
  private final TrackSearchQueryAnalysisService trackSearchQueryAnalysisService;
  private final EmbeddingService embeddingService;
  private final QdrantClient qdrantClient;
  private final TrackRepository trackRepository;

  public List<TrackSemanticSearchItemDto> search(String query,int limit){

    TrackSearchQueryAnalysisResult analysis =
        trackSearchQueryAnalysisService.analyze(query);

    String searchText = buildSearchText(analysis);

    List<Float> vector = embeddingService.embedText(searchText);

    QdrantSearchResponse response = qdrantClient.searchSong(vector,limit);

    List<TrackSemanticSearchItemDto> results = new ArrayList<>();
    if (response == null
        || response.getResult() == null
        || response.getResult().getPoints() == null) {
      return results;
    }
    for(Point point:response.getResult().getPoints()){

      Long trackId = extractTrackId(point);
      if(trackId == null){
        continue;
      }

      Track track = trackRepository.findById(trackId).orElse(null);
      if(track == null){
        continue;
      }

      results.add(TrackSemanticSearchItemDto.from(track, point.getScore()));
    }
    return results;
  }

//  public List<TrackSemanticSearchItemDto> signupSearch(String query,int limit){
//
//  }



  private Long extractTrackId(QdrantSearchResponse.Point point) {
    if (point.getPayload() != null && point.getPayload().getTrackId() != null) {
      return point.getPayload().getTrackId();
    }

    Object id = point.getId();
    if (id instanceof Number number) {
      return number.longValue();
    }

    try {
      return Long.parseLong(String.valueOf(id));
    } catch (Exception e) {
      return null;
    }
  }

  private String buildSearchText(TrackSearchQueryAnalysisResult result) {
    return String.format(
        "mood: %s, scene: %s, texture: %s, genre: %s. description: %s.",
        safeJoin(result.getMood_tags()),
        safeJoin(result.getScene_tags()),
        safeJoin(result.getTexture_tags()),
        safeJoin(result.getGenre_tags()),
        nullToEmpty(result.getShort_description())
    ).trim().replaceAll("\\s+", " ");
  }

  private String safeJoin(List<String> values) {
    return values == null || values.isEmpty() ? "" : String.join(", ", values);
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
