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

  public UserTasteProfileResult generateProfile(String userInput) {
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
                  "text": "You analyze balance game answers and generate a stable music taste profile for recommendation. Focus on long-term listening preference, not temporary mood. Produce practical tags that are useful for music retrieval, embedding-based search, reranking, and onboarding recommendations. Infer distinctive but still believable preferences from the combination of answers.\\\\n\\\\nField language rules:\\\\n- preferred_mood_tags, preferred_scene_tags, preferred_texture_tags, preferred_genre_tags, disliked_tags must be in English only.\\\\n- profile_summary, profile_type_name, and profile_one_liner must be in Korean only.\\\\n\\\\nTag rules:\\\\n- Keep tags short, lowercase, reusable, and recommendation-oriented.\\\\n- Avoid poetic tag phrases, near-duplicate synonyms, and overly niche assumptions unless strongly supported.\\\\n- Do not output broad filler tags that say almost nothing.\\\\n\\\\nCore interpretation rules:\\\\n- Do NOT restate or paraphrase individual answers.\\\\n- Do NOT simply summarize obvious preferences.\\\\n- Use patterns across multiple answers to make light interpretation.\\\\n- The result should feel like: '맞는 말인데, 이렇게 말하니까 조금 새롭다.'\\\\n- Keep the interpretation grounded and believable. Do not make wild claims.\\\\n- Add mild inference, but only when it is reasonably supported by multiple answers.\\\\n\\\\nRules for profile_summary:\\\\n- profile_summary must be written in Korean.\\\\n- Write 2 to 3 sentences.\\\\n- Each sentence must connect personality or behavioral tendency to music style in one flow.\\\\n- Preferred structure: [personality / tendency / way of reacting] -> [music preference / listening style].\\\\n- Example structure only: '혼자 있는 시간이 더 편해서, 음악도 조용한 쪽에 더 끌려요.'\\\\n- Focus on behavior and tendency, not labels.\\\\n- Use indirect and natural phrasing such as '~쪽에 더 끌려요', '~을 더 찾게 돼요', '~한 음악을 자주 듣는 편이에요', '~고르게 되는 편이에요'.\\\\n- Avoid definitive overstatements.\\\\n- Avoid generic clichés and predictable emotional descriptions.\\\\n- Avoid repeating the same sentence ending or pattern. Do not repeat forms like '~하는 편이라', '~선호해요', '~좋아해요' over and over. Vary sentence rhythm and endings naturally.\\\\n- Keep the Korean tone concise, clear, calm, slightly warm, and friendly.\\\\n- Avoid stiff, overly formal, exaggerated, or overly poetic Korean.\\\\n\\\\nRules for profile_type_name:\\\\n- profile_type_name must be written in Korean.\\\\n- Make it short, clear, memorable, and slightly characteristic.\\\\n- It should feel like a personality-test-style result title.\\\\n- Avoid generic labels like '청취자', '애호가', '음악 lover', or empty category words.\\\\n- Prefer natural Korean phrases that feel a little distinctive but still understandable.\\\\n- Do not make it too dramatic, fantasy-like, or cringeworthy.\\\\n\\\\nRules for profile_one_liner:\\\\n- profile_one_liner must be written in Korean.\\\\n- It should be one short memorable sentence.\\\\n- It should not repeat the summary directly.\\\\n- It should feel neat, lightly characterful, and easy to relate to.\\\\n- Keep the tone warm, simple, and not overly cute or exaggerated.\\\\n\\\\nOutput rules:\\\\n- Return only the structured result.\\\\n- Follow the schema strictly.\\\\n- Do not include explanations, markdown, or extra text outside the schema."
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
                  },
                  "profile_type_name": {
                    "type": "string",
                     "maxLength": 40
                  },
                  "profile_one_liner": {
                    "type": "string",
                    "maxLength": 160
                  }
                },
                "required": [
                  "preferred_mood_tags",
                  "preferred_scene_tags",
                  "preferred_texture_tags",
                  "preferred_genre_tags",
                  "disliked_tags",
                  "profile_summary",
                  "profile_type_name",
                  "profile_one_liner"
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
      throw new RuntimeException(
          "Failed to parse OpenAI user taste profile response: " + responseBody, e);
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
