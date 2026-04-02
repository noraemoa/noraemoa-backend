package com.in28minutes.webservices.songrec.dto.response.user;

import com.in28minutes.webservices.songrec.dto.request.TrackSemanticSearchItemDto;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class UserTasteProfileResponseDto {

  private List<TrackSemanticSearchItemDto> tracks;
  private String profileSummary;
  private String profileTypeName;
  private String profileOneLiner;

  public static UserTasteProfileResponseDto from(List<TrackSemanticSearchItemDto> tracks,
      UserTasteProfileResult result) {
    return UserTasteProfileResponseDto.builder()
        .tracks(tracks)
        .profileSummary(result.getProfile_summary())
        .profileTypeName(result.getProfile_type_name())
        .profileOneLiner(result.getProfile_one_liner())
        .build();
  }
}
