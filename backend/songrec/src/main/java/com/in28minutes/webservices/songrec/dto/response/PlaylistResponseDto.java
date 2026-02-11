package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Playlist;
import com.in28minutes.webservices.songrec.domain.Request;
import io.micrometer.common.lang.Nullable;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PlaylistResponseDto {
    private Long id;
    private String code;
    private String title;
    private String thumbnailUrl;

    public static PlaylistResponseDto from(Playlist playlist){
        String code = (playlist.getTemplate() == null)?null:playlist.getTemplate().getCode();
        return PlaylistResponseDto.builder()
                .id(playlist.getId())
                .code(code)
                .title(playlist.getTitle())
                .thumbnailUrl(playlist.getThumbnailUrl())
                .build();
    }
}
