package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationInput;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiTrackTagClient {
  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public TrackTagGenerationResult generateTags(TrackTagGenerationInput input){
    String requestBody=buildRequestBody(input);
    String response=openAiWebClient.post()
        .uri("/responses")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();

    System.out.println("=== OpenAI track tag raw response ===");
    System.out.println(response);
    return parseResponse(response);
  }

  private String buildUserInput(TrackTagGenerationInput input) {
    return """
      Generate conservative search-oriented tags from this track metadata.
      Use only this metadata as evidence.

      Track metadata:
      %s
      """.formatted(toJsonString(input));
  }

  private String buildRequestBody(TrackTagGenerationInput input) {
    String userInput = buildUserInput(input);

    return """
        {
          "model": "%s",
          "input": [
            {
              "role": "system",
              "content": [
                {
                  "type": "input_text",
                  "text": "You generate conservative search-oriented music tags from limited track metadata. Use only the provided metadata as evidence. Do not invent unsupported facts. If metadata is insufficient, prefer broad and conservative tags. Do not guess BPM, detailed instrumentation, exact vocal type, or highly specific subgenres unless clearly supported. Tags must be practical for search and recommendation. Return only the structured result."
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
              "name": "track_tag_generation",
              "strict": true,
              "schema": {
                "type": "object",
                "properties": {
                  "mood_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 6
                  },
                  "scene_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 6
                  },
                  "texture_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 6
                  },
                  "short_description": {
                    "type": "string",
                    "maxLength": 200
                  },
                  "confidence_note": {
                    "type": "string",
                    "maxLength": 200
                  }
                },
                "required": [
                  "mood_tags",
                  "scene_tags",
                  "texture_tags",
                  "short_description",
                  "confidence_note"
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

  private TrackTagGenerationResult parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);

      String outputText = extractOutputText(root);
      if (outputText == null || outputText.isBlank()) {
        throw new RuntimeException("OpenAI response missing text output. raw=" + responseBody);
      }

      return objectMapper.readValue(outputText, TrackTagGenerationResult.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI track tag response. raw=" + responseBody, e);
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
        String type = contentItem.path("type").asText();

        if ("output_text".equals(type)) {
          String text = contentItem.path("text").asText(null);
          if (text != null && !text.isBlank()) {
            return text;
          }
        }

        // 혹시 text 필드만 오는 경우 대비
        if (contentItem.has("text")) {
          String text = contentItem.path("text").asText(null);
          if (text != null && !text.isBlank()) {
            return text;
          }
        }
      }
    }

    return null;
  }

}
