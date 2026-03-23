package com.in28minutes.webservices.songrec.integration.openai.dto;
import com.in28minutes.webservices.songrec.domain.keyword.Keyword;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestPromptRefineResult {
  private String title;
  private List<String> keywords;

  public static RequestPromptRefineResult from(String title,List<String> keywords){

    return RequestPromptRefineResult.builder()
        .title(title)
        .keywords(keywords).build();
  }
}
