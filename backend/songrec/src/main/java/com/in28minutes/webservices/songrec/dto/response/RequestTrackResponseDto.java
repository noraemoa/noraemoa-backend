package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestTrackResponseDto {
    private Long id;
    private Long requestId;
    private Long trackId;

    public static RequestTrackResponseDto from(RequestTrack requestTrack){
        return RequestTrackResponseDto.builder()
                .id(requestTrack.getId())
                .requestId(requestTrack.getRequest().getId())
                .trackId(requestTrack.getTrack().getId())
                .build();
    }
}
