package com.in28minutes.webservices.songrec.integration.openai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenAiImageGenerateRequest {
  private String model;
  private String prompt;
  private String size;
  private String quality;
  private String output_format;
  private String background;
  private Integer n;
}
