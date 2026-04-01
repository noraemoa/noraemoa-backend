package com.in28minutes.webservices.songrec.service.openai;

import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.integration.openai.client.OpenAiUserTasteProfileClient;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserTasteProfileService {
  private final OpenAiUserTasteProfileClient openAiUserTasteProfileClient;

  public UserTasteProfileResult generateProfile(UserTasteProfileCreateRequestDto dto){
    String userInput = buildUserInput(dto);
    return openAiUserTasteProfileClient.generateProfile(userInput);
  }

  private String buildUserInput(UserTasteProfileCreateRequestDto dto) {
    String answers = dto.getAnswers().stream()
        .map(a -> "- " + a.getQuestionKey() + ": " + a.getChoiceKey())
        .collect(Collectors.joining("\n"));

    return """
        Generate a stable music taste profile from these balance game answers.

        Answers:
        %s
        """.formatted(answers);
  }
}
