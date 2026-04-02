package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.user.AggregatedTasteProfile;
import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiUserTasteProfileClient;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTasteProfileService {
  private final OpenAiUserTasteProfileClient openAiUserTasteProfileClient;

  public UserTasteProfileResult generateProfile(UserTasteProfileCreateRequestDto dto,
      AggregatedTasteProfile aggregatedTasteProfile){
    String userInput = buildUserInput(dto, aggregatedTasteProfile);
    return openAiUserTasteProfileClient.generateProfile(userInput);
  }

  private String buildUserInput(UserTasteProfileCreateRequestDto dto,AggregatedTasteProfile aggregatedTasteProfile) {
    String answers = dto.getAnswers().stream()
        .map(a -> """
            Question: %s
            Selected: %s
            """.formatted(a.getQuestionText(),a.getChoiceText()))
        .collect(Collectors.joining("\n\n"));

    return """
        Build a stable music preference profile from the following balance game answers.
        
        Important:
        - Infer patterns across multiple answers, not one-to-one restatement.
        - Make the Korean text fields feel like a personality test result that is relatable, slightly fresh, and believable.
        - Keep the result grounded.

        Answers:
        %s
        
        Rule-based base tags:
        - mood: %s
        - scene: %s
        - texture: %s
        - genre: %s
        - disliked: %s
        
        Base summary:
        %s
        """.formatted(
            answers,
        String.join(", ",aggregatedTasteProfile.getMoodTags()),
        String.join(", ",aggregatedTasteProfile.getSceneTags()),
        String.join(", ",aggregatedTasteProfile.getTextureTags()),
        String.join(", ",aggregatedTasteProfile.getGenreTags()),
        String.join(", ",aggregatedTasteProfile.getDislikedTags()),
        aggregatedTasteProfile.getBaseSummary()
    );
  }
}
