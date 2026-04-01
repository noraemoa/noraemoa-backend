package com.in28minutes.webservices.songrec.service.qdrant;

import com.in28minutes.webservices.songrec.integration.qdrant.client.QdrantClient;
import com.in28minutes.webservices.songrec.integration.qdrant.config.QdrantProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QdrantCollectionService {
  private final QdrantClient qdrantClient;
  private final QdrantProperties qdrantProperties;

  public void createSongsCollectionIfNotExists() {
    qdrantClient.createCollectionIfNotExist(qdrantProperties.getCollectionName());
  }

  public void ensureUserProfileCollection(){
    qdrantClient.createCollectionIfNotExist("user_profiles");
  }
}
