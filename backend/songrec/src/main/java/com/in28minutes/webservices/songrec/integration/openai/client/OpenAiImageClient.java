package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.GeneratedImageResult;
import com.in28minutes.webservices.songrec.integration.openai.dto.OpenAiImageGenerateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OpenAiImageClient {

  // OpenAI API 호출용 (Webclient 외부 HTTP API 호출용)
  private final WebClient openAiWebClient;
  private final ObjectMapper objectMapper;

  public GeneratedImageResult generate(String prompt) {

    OpenAiImageGenerateRequest request = OpenAiImageGenerateRequest.builder()
        .model("gpt-image-1.5")
        .prompt(prompt)
        .quality("medium")
        .output_format("png")
        .background("opaque")
        .n(1)
        .build();

    String response = openAiWebClient.post()
        .uri("/images/generations")
        .bodyValue(request)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .map(body -> new RuntimeException("OpenAI Image API error: " + body)))
        .bodyToMono(String.class).block();

    return parse(response);
  }

  private String toJson(String value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private GeneratedImageResult parse(String response) {
    try {
      JsonNode root = objectMapper.readTree(response);
      JsonNode data = root.path("data");
      if (!data.isArray() || data.isEmpty()) {
        throw new RuntimeException("OpenAI image response missing data array: " + response);
      }

      JsonNode first = data.get(0);
      String base64 = first.path("b64_json").asText(null);
      if (base64 == null || base64.isBlank()) {
        throw new RuntimeException("OpenAI image response missing b64_json: " + response);
      }

      byte[] imageBytes = Base64.getDecoder().decode(base64);

      return new GeneratedImageResult(imageBytes, "image/png");
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse image response", e);
    }
  }
}