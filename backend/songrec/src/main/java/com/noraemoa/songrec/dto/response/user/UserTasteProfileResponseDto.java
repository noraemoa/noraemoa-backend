package com.noraemoa.songrec.dto.response.user;

import com.noraemoa.songrec.dto.request.TrackSemanticSearchItemDto;
import com.noraemoa.songrec.integration.openai.dto.UserTasteProfileResult;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

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
