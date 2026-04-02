package com.in28minutes.webservices.songrec.dto.response.user;

import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class UserTasteProfileResponseDto {
  private String profileSummary;
  private String profileTypeName;
  private String profileOneLiner;

  public static UserTasteProfileResponseDto from(UserTasteProfileResult result){
    return UserTasteProfileResponseDto.builder()
        .profileSummary(result.getProfile_summary())
        .profileTypeName(result.getProfile_type_name())
        .profileOneLiner(result.getProfile_one_liner())
        .build();
  }
}
