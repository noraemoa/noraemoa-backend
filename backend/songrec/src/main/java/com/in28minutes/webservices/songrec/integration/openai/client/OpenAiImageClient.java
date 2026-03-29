package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.GeneratedImageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Component
@RequiredArgsConstructor
public class OpenAiImageClient {

  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public GeneratedImageResult generate(String prompt) {

    String requestBody = """
        {
          "model": "gpt-image-1.5",
          "prompt": %s,
          "size": "1024x1024",
          "quality": "medium",
            "output_format": "png"
        }
        """.formatted(toJson(prompt));

    System.out.println("=== OpenAI image request body ===");
    System.out.println(requestBody);

    String response = openAiWebClient.post()
        .uri("/images/generations")
        .header("Authorization", "Bearer " + properties.getApiKey())
        .header("Content-Type", "application/json")
        .bodyValue(requestBody)
        .retrieve()
        .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
            clientResponse -> clientResponse.bodyToMono(String.class)
                .map(body -> new RuntimeException("OpenAI Image API error: " + body)))
        .bodyToMono(String.class).block();

    System.out.println("=== OpenAI image raw response ===");
    System.out.println(response);
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
      String base64 = root.path("data").get(0).path("b64_json").asText();

      byte[] imageBytes = Base64.getDecoder().decode(base64);

      return new GeneratedImageResult(imageBytes, "image/png");
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse image response", e);
    }
  }
}