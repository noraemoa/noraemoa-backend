package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QdrantUserProfileRetrieveRequest {
  private List<String> ids;
  private Boolean with_payload;
  private Boolean with_vector;
}
