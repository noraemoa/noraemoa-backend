package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QueryEmbeddingPayload {
  private Long requestId;
  private String queryText;
}
