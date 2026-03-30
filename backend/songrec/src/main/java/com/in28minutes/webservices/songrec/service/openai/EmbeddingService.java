package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiEmbeddingClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmbeddingService {
  private final OpenAiEmbeddingClient openAiEmbeddingClient;

  public List<Float> embedText(String text){
    if(text == null || text.isEmpty()){
      throw new IllegalArgumentException("text is null or empty");
    }
    return openAiEmbeddingClient.embed(text);
  }
}
