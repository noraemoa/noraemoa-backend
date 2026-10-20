package com.noraemoa.songrec.service.openai;

import com.noraemoa.songrec.integration.openai.client.OpenAiRequestPromptClient;
import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.noraemoa.songrec.service.RequestPromptAiService;
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
