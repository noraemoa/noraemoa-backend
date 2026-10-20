package com.noraemoa.songrec.service.openai;

import com.noraemoa.songrec.integration.openai.client.OpenAiTrackTagClient;
import com.noraemoa.songrec.integration.openai.dto.TrackTagGenerationInput;
import com.noraemoa.songrec.integration.openai.dto.TrackTagGenerationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class TrackTagGenerationService {
  private final OpenAiTrackTagClient openAiTrackTagClient;

  public TrackTagGenerationResult generateTags(TrackTagGenerationInput input){
    return openAiTrackTagClient.generateTags(input);
  }
}
