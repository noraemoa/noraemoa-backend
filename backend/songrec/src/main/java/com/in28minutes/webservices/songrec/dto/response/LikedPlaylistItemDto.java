package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.repository.projection.LikedPlaylistRow;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class LikedPlaylistItemDto {
  private Long playlistId;
  private String username;
  private String playlistTitle;
  private String thumbnailUrl;
  private LocalDateTime createdAt;

  public static LikedPlaylistItemDto from(LikedPlaylistRow playlistRow) {
    return LikedPlaylistItemDto.builder()
        .playlistId(playlistRow.getPlaylistId())
        .username(playlistRow.getUsername())
        .playlistTitle(playlistRow.getPlaylistTitle())
        .thumbnailUrl(playlistRow.getThumbnailUrl())
        .createdAt(playlistRow.getCreatedAt())
        .build();
  }
}
