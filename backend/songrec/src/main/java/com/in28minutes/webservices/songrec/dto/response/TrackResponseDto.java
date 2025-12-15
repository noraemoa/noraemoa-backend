package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Track;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TrackResponseDto {
    private Long trackId;
    private String spotifyId;
    private String name;
    private String artist;
    private String album;

    public static TrackResponseDto from(Track track) {
        return TrackResponseDto.builder()
                .trackId(track.getId())
                .spotifyId(track.getSpotifyId())
                .name(track.getName())
                .artist(track.getArtist())
                .album(track.getAlbum())
                .build();
    }
}
