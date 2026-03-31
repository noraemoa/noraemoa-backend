package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QdrantSearchRequest {
  private List<Float> query;
  private Integer limit;
  private Boolean with_payload;
  private Boolean with_vector;
  private Map<String,Object> filter;
}
