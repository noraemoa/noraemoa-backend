package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QdrantRetrieveRequest {
  private List<Long> ids;
  private Boolean with_payload;
  private Boolean with_vector;
}
