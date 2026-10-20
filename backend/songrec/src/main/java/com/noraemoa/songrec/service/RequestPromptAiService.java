package com.noraemoa.songrec.service;

import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;

public interface RequestPromptAiService {
  RequestPromptRefineResult refine(String prompt);
}
