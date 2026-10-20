package com.noraemoa.songrec.repository.projection;

import com.noraemoa.songrec.domain.playlist.PlaylistVisibility;

public interface PopularPlaylistRow {
  Long getPlaylistId();
  Long getUserId();
  String getUsername();
  String getTitle();
  String getThumbnailUrl();
  PlaylistVisibility getVisibility();
  Integer getLikeCount();
}
