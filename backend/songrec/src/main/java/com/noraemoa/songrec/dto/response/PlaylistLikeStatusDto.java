package com.noraemoa.songrec.dto.response;

import com.noraemoa.songrec.domain.like.PlaylistLike;
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
