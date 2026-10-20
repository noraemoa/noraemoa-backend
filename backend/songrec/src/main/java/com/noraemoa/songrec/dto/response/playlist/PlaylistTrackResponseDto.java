package com.noraemoa.songrec.dto.response.playlist;

import com.noraemoa.songrec.domain.playlist.PlaylistTrack;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaylistTrackResponseDto {
    private Long id;
    private Long playlistId;
    private Long trackId;

    public static PlaylistTrackResponseDto from(PlaylistTrack playlistTrack){
        return PlaylistTrackResponseDto.builder()
                .id(playlistTrack.getId())
                .playlistId(playlistTrack.getPlaylist().getId())
                .trackId(playlistTrack.getTrack().getId())
                .build();
    }
}
