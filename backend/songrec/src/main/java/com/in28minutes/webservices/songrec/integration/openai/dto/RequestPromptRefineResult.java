package com.in28minutes.webservices.songrec.integration.openai.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestPromptRefineResult {
  private String title;
  private List<String> keywords;
}
