package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiRequestPromptClient;
import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiRequestPromptAiService implements RequestPromptAiService {
  private final OpenAiRequestPromptClient openAiRequestPromptClient;

  @Override
  public RequestPromptRefineResult refine(String prompt){
    return openAiRequestPromptClient.refinePrompt(prompt);
  }
}
