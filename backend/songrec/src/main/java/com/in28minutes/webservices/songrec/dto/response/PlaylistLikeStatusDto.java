package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.like.PlaylistLike;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class PlaylistLikeStatusDto {
  private Long playlistId;
  private boolean liked;

  public static PlaylistLikeStatusDto from(PlaylistLike playlistLike) {
    return PlaylistLikeStatusDto.builder()
        .playlistId(playlistLike.getPlaylist().getId())
        .liked(true)
        .build();
  }
}
