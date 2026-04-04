package com.in28minutes.webservices.songrec.dto.response.request;

import com.in28minutes.webservices.songrec.domain.request.RequestTrackRating;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestTrackRatingResponseDto {

  private Long requestId;
  private Long userId;
  private Long trackId;
  private Integer rating;

  public static RequestTrackRatingResponseDto from(RequestTrackRating requestTrackRating) {
    return RequestTrackRatingResponseDto.builder()
        .requestId(requestTrackRating.getRequestTrack().getRequest().getId())
        .trackId(requestTrackRating.getRequestTrack().getTrack().getId())
        .userId(requestTrackRating.getUser().getId())
        .rating(requestTrackRating.getRating())
        .build();
  }
}
