package com.in28minutes.webservices.songrec.integration.openai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.integration.openai.config.OpenAiProperties;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackSearchQueryAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OpenAiTrackSearchQueryClient {
  private final WebClient openAiWebClient;
  private final OpenAiProperties properties;
  private final ObjectMapper objectMapper;

  public TrackSearchQueryAnalysisResult analyze(String query) {
    String requestBody = buildRequestBody(query);
    String response = openAiWebClient.post()
        .uri("/responses")
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(String.class)
        .block();
    return parseResponse(response);
  }

  private String buildRequestBody(String query) {
    String userInput = buildUserInput(query);

    return """
        {
          "model": "%s",
          "input": [
            {
              "role": "system",
              "content": [
                {
                  "type": "input_text",
                  "text": "You convert a user's music search request into conservative search-oriented tags. Focus on search usefulness. Keep tags short and practical. Infer only what is strongly implied by the request. Do not invent unrelated genres or moods. Include exclude_tags only when the user clearly excludes something. language should be ko, en, or unknown when appropriate. Return only the structured result.Different user requests should produce meaningfully different search tags when the mood, scene, or intent differs.Prioritize scene and mood distinctions strongly.Do not collapse distinct requests into generic tags like calm, pop, or casual listening unless the request is truly generic."
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
              "name": "track_search_query_analysis",
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
                  "genre_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 5
                  },
                  "exclude_tags": {
                    "type": "array",
                    "items": { "type": "string" },
                    "maxItems": 5
                  },
                  "language": {
                    "type": "string"
                  },
                  "short_description": {
                    "type": "string",
                    "maxLength": 200
                  }
                },
                "required": [
                  "mood_tags",
                  "scene_tags",
                  "texture_tags",
                  "genre_tags",
                  "exclude_tags",
                  "language",
                  "short_description"
                ],
                "additionalProperties": false
              }
            }
          }
        }
        """.formatted(
        properties.getModel(),
        toJsonString(userInput)
    );
  }
  private String buildUserInput(String query) {
    return """
        Analyze this music search request and convert it into structured retrieval tags.

        User query:
        %s
        """.formatted(query);
  }

  private String toJsonString(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize value", e);
    }
  }

  private TrackSearchQueryAnalysisResult parseResponse(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      String outputText = extractOutputText(root);

      if (outputText == null || outputText.isBlank()) {
        throw new RuntimeException("Missing output text in OpenAI response: " + responseBody);
      }

      return objectMapper.readValue(outputText, TrackSearchQueryAnalysisResult.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse OpenAI search query analysis response: " + responseBody, e);
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
