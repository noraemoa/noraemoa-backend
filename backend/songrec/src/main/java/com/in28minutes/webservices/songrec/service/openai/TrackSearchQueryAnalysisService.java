package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiTrackSearchQueryClient;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrackSearchQueryAnalysisService {
  private final OpenAiTrackSearchQueryClient openAiTrackSearchQueryClient;

  public TrackSearchQueryAnalysisResult analyze(String query) {
    if(query == null || query.isEmpty()) {
      throw new IllegalArgumentException("query is null or empty");
    }
    return openAiTrackSearchQueryClient.analyze(query);
  }
}
