package com.noraemoa.songrec.dto.response.track;

import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.integration.spotify.dto.SpotifyTrackResponseDto.SpotifyTrack;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TrackResponseDto {
    private Long trackId;
    private SpotifyTrack spotifyTrack;

    public static TrackResponseDto from(Track track,boolean liked) {
        var spotify = SpotifyTrack.builder()
            .spotifyId(track.getSpotifyId())
            .name(track.getName())
            .artistName(track.getArtist())
            .album(track.getAlbum())
            .imageUrl(track.getImageUrl())
            .durationMs(track.getDurationMs())
            .liked(liked)
            .build();
        return TrackResponseDto.builder()
                .trackId(track.getId())
                .spotifyTrack(spotify)
                .build();
    }
}
