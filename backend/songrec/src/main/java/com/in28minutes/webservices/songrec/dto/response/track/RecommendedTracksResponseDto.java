package com.in28minutes.webservices.songrec.dto.response.track;

import com.in28minutes.webservices.songrec.dto.response.TrackSimpleResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RecommendedTracksResponseDto {
    private List<TrackSimpleResponseDto> tracks;
    private boolean hasNext;
    private int nextPage;
}
