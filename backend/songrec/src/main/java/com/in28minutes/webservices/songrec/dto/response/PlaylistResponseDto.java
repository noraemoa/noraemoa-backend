package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaylistResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String code;
    private String title;
    private String thumbnailUrl;
    private PlaylistVisibility visibility;

    public static PlaylistResponseDto from(Playlist playlist){
        String code = (playlist.getTemplate() == null)?null:playlist.getTemplate().getCode();
        return PlaylistResponseDto.builder()
                .id(playlist.getId())
                .userId(playlist.getUser().getId())
                .username(playlist.getUser().getUsername())
                .code(code)
                .title(playlist.getTitle())
                .thumbnailUrl(playlist.getThumbnailUrl())
                .visibility(playlist.getVisibility())
                .build();
    }
}
