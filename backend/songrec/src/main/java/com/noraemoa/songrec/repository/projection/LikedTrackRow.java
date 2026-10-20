package com.noraemoa.songrec.repository.projection;

import java.time.LocalDateTime;

public interface LikedTrackRow {
  Long getTrackId();
  String getName();
  String getArtist();
  String getAlbum();
  String getImageUrl();
  LocalDateTime getCreatedAt();
}
