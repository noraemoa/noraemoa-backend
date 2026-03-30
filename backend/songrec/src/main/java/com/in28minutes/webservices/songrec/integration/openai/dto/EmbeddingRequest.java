package com.in28minutes.webservices.songrec.integration.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmbeddingRequest {
  private String model;
  private Object input;
  private String encoding_format;
  private Integer dimensions;
}
