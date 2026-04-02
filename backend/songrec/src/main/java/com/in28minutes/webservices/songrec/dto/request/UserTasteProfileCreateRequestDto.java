package com.in28minutes.webservices.songrec.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTasteProfileCreateRequestDto {

  @NotEmpty
  private List<BalanceAnswerDto> answers;

  @Getter @Setter
  public static class BalanceAnswerDto{
    private String questionText;
    private String choiceText;
    private String choiceKey;
  }
}
