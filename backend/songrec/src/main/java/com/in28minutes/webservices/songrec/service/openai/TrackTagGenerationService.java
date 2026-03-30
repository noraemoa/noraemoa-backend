package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiTrackTagClient;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationInput;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackTagGenerationService {
  private final OpenAiTrackTagClient openAiTrackTagClient;

  public TrackTagGenerationResult generateTags(TrackTagGenerationInput input){
    return openAiTrackTagClient.generateTags(input);
  }
}
