package com.noraemoa.songrec.repository.projection;

import java.time.LocalDateTime;

public interface LikedPlaylistRow {
  Long getPlaylistId();
  String getUsername();
  String getPlaylistTitle();
  String getThumbnailUrl();
  LocalDateTime getCreatedAt();
}
