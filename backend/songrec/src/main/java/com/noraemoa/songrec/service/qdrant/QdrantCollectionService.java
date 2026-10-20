package com.noraemoa.songrec.service.qdrant;

import com.noraemoa.songrec.integration.qdrant.client.QdrantClient;
import com.noraemoa.songrec.integration.qdrant.config.QdrantProperties;
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

  public void ensureQueryEmbeddingCollection(){
    qdrantClient.createCollectionIfNotExist("query_embeddings");
  }
}
