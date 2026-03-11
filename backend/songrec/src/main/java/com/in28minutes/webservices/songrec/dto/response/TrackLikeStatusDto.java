package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.like.TrackLike;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class TrackLikeStatusDto {
  private Long trackId;
  private boolean liked;
  public static TrackLikeStatusDto from(TrackLike tl){
    return TrackLikeStatusDto.builder()
        .trackId(tl.getTrack().getId())
        .liked(true)
        .build();
  }
}
