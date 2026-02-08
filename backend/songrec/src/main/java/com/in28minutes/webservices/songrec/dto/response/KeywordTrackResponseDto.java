package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class KeywordTrackResponseDto {
    private Long id;
    private Long keywordId;
    private Long trackId;
    private Integer recommendCount;
    private Integer ratingCount;

    public static KeywordTrackResponseDto from(KeywordTrack keywordTrack) {
        return KeywordTrackResponseDto.builder()
                .id(keywordTrack.getId())
                .keywordId(keywordTrack.getKeyword().getId())
                .trackId(keywordTrack.getTrack().getId())
                .recommendCount(keywordTrack.getRecommendCount())
                .ratingCount(keywordTrack.getRatingCount())
                .build();
    }

}
