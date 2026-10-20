package com.noraemoa.songrec.config;

import com.noraemoa.songrec.service.qdrant.QdrantCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QdrantInitializer implements CommandLineRunner {
  private final QdrantCollectionService qdrantCollectionService;

  @Override
  public void run(String... args){
    qdrantCollectionService.ensureQueryEmbeddingCollection();
  }
}
