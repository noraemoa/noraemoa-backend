package com.in28minutes.webservices.songrec.integration.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneratedImageResult {
  private byte[] imageBytes;
  private String contentType;
}
