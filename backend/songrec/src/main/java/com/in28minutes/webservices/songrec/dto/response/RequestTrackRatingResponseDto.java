package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.RequestTrack;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestTrackRatingResponseDto {
    private Long id;
    private Long requestId;
    private Long trackId;
    private Integer rating;

    public static RequestTrackRatingResponseDto from(RequestTrack requestTrack){
        return RequestTrackRatingResponseDto.builder()
                .id(requestTrack.getId())
                .requestId(requestTrack.getRequest().getId())
                .trackId(requestTrack.getTrack().getId())
                .rating(requestTrack.getRating())
                .build();
    }
}
