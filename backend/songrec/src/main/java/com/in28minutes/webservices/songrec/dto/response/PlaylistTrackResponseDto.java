package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.PlaylistTrack;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
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
