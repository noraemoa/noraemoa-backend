package com.in28minutes.webservices.songrec.service.qdrant;

import com.in28minutes.webservices.songrec.integration.qdrant.client.QdrantClient;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantPoint;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.UserProfilePayload;
import com.in28minutes.webservices.songrec.service.openai.EmbeddingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileVectorIndexService {
  private final QdrantClient qdrantClient;
  private final EmbeddingService embeddingService;

  public Long upsertUserProfile(UserProfilePayload payload) {
    List<Float> vector = embeddingService.embedText(payload.getProfile_summary());

    Long pointId = payload.getUserId();

    QdrantPoint point = QdrantPoint.builder()
        .id(pointId)
        .vector(vector)
        .payload(payload)
        .build();

    qdrantClient.upsertUserProfilePoint(point);

    return pointId;
  }

}
