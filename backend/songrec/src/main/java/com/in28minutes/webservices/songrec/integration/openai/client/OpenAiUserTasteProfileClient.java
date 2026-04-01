package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiUserTasteProfileClient {
  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public UserTasteProfileResult generateProfile(String userInput){
    String requestBody = buildRequestBody(userInput);

    String response = openAiWebClient.post()
        .uri("/responses")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    return parseResponse(response);
  }

  private String buildRequestBody(String userInput) {
    return """
        {
          "model": "%s",
          "input": [
            {
              "role": "system",
              "content": [
                {
                  "type": "input_text",
                  "text": "You generate a stable music preference profile from balance game answers. Return practical recommendation-oriented tags, not literary descriptions. Keep tags short. Infer broad preferences only. Return only the structured result."
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
              "name": "user_taste_profile",
              "strict": true,
              "schema": {
                "type": "object",
                "properties": {
                  "preferred_mood_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 8
                  },
                  "preferred_scene_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 8
                  },
                  "preferred_texture_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 8
                  },
                  "preferred_genre_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 6
                  },
                  "disliked_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 6
                  },
                  "profile_summary": {
                    "type": "string",
                    "maxLength": 300
                  }
                },
                "required": [
                  "preferred_mood_tags",
                  "preferred_scene_tags",
                  "preferred_texture_tags",
                  "preferred_genre_tags",
                  "disliked_tags",
                  "profile_summary"
                ],
                "additionalProperties": false
              }
            }
          }
        }
        """.formatted(properties.getModel(), toJsonString(userInput));
  }

  private String toJsonString(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize value", e);
    }
  }

  private UserTasteProfileResult parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      String outputText = extractOutputText(root);

      if (outputText == null || outputText.isBlank()) {
        throw new RuntimeException("Missing output text in OpenAI response: " + responseBody);
      }

      return objectMapper.readValue(outputText, UserTasteProfileResult.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI user taste profile response: " + responseBody, e);
    }
  }

  private String extractOutputText(JsonNode root) {
    String topLevel = root.path("output_text").asText(null);
    if (topLevel != null && !topLevel.isBlank()) {
      return topLevel;
    }

    JsonNode output = root.path("output");
    if (!output.isArray()) {
      return null;
    }

    for (JsonNode item : output) {
      if (!"message".equals(item.path("type").asText())) {
        continue;
      }

      JsonNode content = item.path("content");
      if (!content.isArray()) {
        continue;
      }

      for (JsonNode contentItem : content) {
        String text = contentItem.path("text").asText(null);
        if (text != null && !text.isBlank()) {
          return text;
        }
      }
    }

    return null;
  }
}
