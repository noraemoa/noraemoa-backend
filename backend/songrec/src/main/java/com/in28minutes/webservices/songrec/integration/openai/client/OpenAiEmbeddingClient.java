package com.in28minutes.webservices.songrec.integration.openai.client;

import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.EmbeddingRequest;
import com.in28minutes.webservices.songrec.integration.openai.dto.EmbeddingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiEmbeddingClient {
  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;

  public List<Float> embed(String text){
    EmbeddingRequest request = EmbeddingRequest.builder()
        .model(properties.getEmbeddingModel())
        .input(text)
        .encoding_format("float").build();

    EmbeddingResponse response = openAiWebClient.post()
        .uri("/embeddings")
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError,
            clientResponse ->clientResponse.bodyToMono(String.class)
                .map(body->new RuntimeException("OpenAI embeddings error: "+ body)))
        .bodyToMono(EmbeddingResponse.class)
        .block();

    if (response == null
        || response.getData() == null
        || response.getData().isEmpty()
        || response.getData().get(0).getEmbedding() == null
        || response.getData().get(0).getEmbedding().isEmpty()) {
      throw new RuntimeException("OpenAI embeddings response is empty");
    }

    return response.getData().get(0).getEmbedding();
  }
}
