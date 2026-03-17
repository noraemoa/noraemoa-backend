package com.in28minutes.webservices.songrec.dto.response.playlist;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaylistWithLikedResponseDto {

  private Long id;
  private Long userId;
  private String username;
  private String code;
  private String title;
  private String thumbnailUrl;
  private PlaylistVisibility visibility;
  private boolean liked;


  public static PlaylistWithLikedResponseDto from(Playlist playlist, boolean liked) {

    return PlaylistWithLikedResponseDto.builder()
        .id(playlist.getId())
        .userId(playlist.getUser().getId())
        .username(playlist.getUser().getUsername())
        .code(playlist.getTemplate() == null ? null : playlist.getTemplate().getCode())
        .title(playlist.getTitle())
        .thumbnailUrl(playlist.getThumbnailUrl())
        .visibility(playlist.getVisibility())
        .liked(liked).build();
  }
}
