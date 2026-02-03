package com.in28minutes.webservices.songrec.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RecommendedTracksResponseDto {
    private List<TrackResponseDto> tracks;
    private boolean hasNext;
    private int nextPage;
}
