package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiRequestPromptClient {

  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public RequestPromptRefineResult refinePrompt(String prompt) {

    String requestBody = buildRequestBody(prompt);

    String response = openAiWebClient.post()
        .uri("/responses")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    return parseResponse(response);
  }

  private String buildRequestBody(String prompt) {
    return """
        {
          "model": "%s",
          "input": [
            {
              "role": "system",
              "content": [
                {
                  "type": "input_text",
                  "text": "You convert a user's music recommendation request into: 1. a concise playlist title 2. 3 to 6 search-friendly keywords. Rules: The title must be short and natural. The title must sound like a playlist name, not a sentence. Keywords must be useful for music recommendation or search. Avoid duplicates. Preserve the user's language when appropriate. Return valid JSON only."
                }
              ]
            },
            {
              "role": "user",
              "content": [
                {
                  "type": "input_text",
                  "text": %s
                }
              ]
            }
          ],
          "text": {
            "format": {
              "type": "json_schema",
              "name": "request_prompt_refine",
              "strict": true,
              "schema": {
                          "type": "object",
                          "properties": {
                            "title": {
                              "type": "string",
                              "minLength": 1,
                              "maxLength": 40
                            },
                            "keywords": {
                              "type": "array",
                              "minItems": 3,
                              "maxItems": 6,
                              "items": {
                                "type": "string",
                                "minLength": 1,
                                "maxLength": 30
                              }
                            }
                          },
                          "required": ["title", "keywords"],
                          "additionalProperties": false
                        }
            }
          }
        }
        """.formatted(properties.getModel(), toJsonString(prompt));
  }

  private String toJsonString(String value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize prompt", e);
    }
  }

  private RequestPromptRefineResult parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);

      String outputText = extractOutputText(root);
      JsonNode json = objectMapper.readTree(outputText);

      String title = json.path("title").asText();
      List<String> keywords = new ArrayList<>();
      for (JsonNode keyword : json.path("keywords")) {
        keywords.add(keyword.asText());
      }

      return RequestPromptRefineResult.from(title, keywords);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI response", e);
    }
  }

  private String extractOutputText(JsonNode root) {
    String outputText = root.path("output_text").asText(null);
    if (outputText != null && !outputText.isBlank()) {
      return outputText;
    }

    JsonNode output = root.path("output");
    if (output.isArray()) {
      for (JsonNode item : output) {
        if (!"message".equals(item.path("type").asText())) {
          continue;
        }

        JsonNode content = item.path("content");
        if (!content.isArray()) {
          continue;
        }

        for (JsonNode contentItem : content) {
          if ("output_text".equals(contentItem.path("type").asText())) {
            String text = contentItem.path("text").asText(null);
            if (text != null && !text.isBlank()) {
              return text;
            }
          }
        }
      }
    }

    throw new RuntimeException("OpenAI response missing output text");
  }
}