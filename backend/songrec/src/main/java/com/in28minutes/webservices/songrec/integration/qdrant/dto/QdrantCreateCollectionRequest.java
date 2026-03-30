package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QdrantCreateCollectionRequest {
  private Vectors vectors;

  @Getter
  @Builder
  @AllArgsConstructor
  public static class Vectors{
    private Integer size;
    private String distance;
  }

}
