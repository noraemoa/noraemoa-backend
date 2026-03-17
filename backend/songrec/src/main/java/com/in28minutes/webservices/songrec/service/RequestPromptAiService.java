package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;

public interface RequestPromptAiService {
  RequestPromptRefineResult refine(String prompt);
}
