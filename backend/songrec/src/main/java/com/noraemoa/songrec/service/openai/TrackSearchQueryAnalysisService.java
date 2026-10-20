package com.noraemoa.songrec.service.openai;

import com.noraemoa.songrec.integration.openai.client.OpenAiTrackSearchQueryClient;
import com.noraemoa.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
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
