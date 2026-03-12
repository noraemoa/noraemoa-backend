package com.in28minutes.webservices.songrec.repository.projection;

import java.time.LocalDateTime;

public interface LikedPlaylistRow {
  Long getPlaylistId();
  String getUsername();
  String getPlaylistTitle();
  String getThumbnailUrl();
  LocalDateTime getCreatedAt();
}
