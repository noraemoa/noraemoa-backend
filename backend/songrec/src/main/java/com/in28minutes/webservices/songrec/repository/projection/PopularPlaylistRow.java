package com.in28minutes.webservices.songrec.repository.projection;

import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;

public interface PopularPlaylistRow {
  Long getPlaylistId();
  Long getUserId();
  String getUsername();
  String getTitle();
  String getThumbnailUrl();
  PlaylistVisibility getVisibility();
  Integer getLikeCount();
}
