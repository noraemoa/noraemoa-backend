package com.in28minutes.webservices.songrec.service.qdrant;

import com.in28minutes.webservices.songrec.integration.qdrant.client.QdrantClient;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantPoint;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.SongPayload;
import com.in28minutes.webservices.songrec.service.openai.EmbeddingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SongVectorIndexService {
  private final QdrantClient qdrantClient;
  private final EmbeddingService embeddingService;

  public void upsertSong(SongPayload payload){
    String searchText = buildSearchText(payload);
    List<Float>embeddingVector=embeddingService.embedText(searchText);
    QdrantPoint point=QdrantPoint.builder()
        .id(payload.getTrackId())
        .vector(embeddingVector)
        .payload(payload).build();

    qdrantClient.upsertSongPoint(point);
  }

  private String buildSearchText(SongPayload payload) {
    return String.format(
        "%s song for %s, %s style, %s by %s",
        safeJoin(payload.getMood_tags()),
        safeJoin(payload.getScene_tags()),
        safeJoin(payload.getTexture_tags()),
        nullToEmpty(payload.getTitle()),
        nullToEmpty(payload.getArtist()),
        nullToEmpty(payload.getShort_description())
    ).trim().replaceAll("\\s+", " ");
  }

  private String safeJoin(List<String> values){
    return values ==null?"":String.join(" ",values);
  }
  private String nullToEmpty(String value){
    return value==null?"":value;
  }
}
