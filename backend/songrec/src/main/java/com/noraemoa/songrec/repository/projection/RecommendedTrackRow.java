package com.noraemoa.songrec.repository.projection;

public interface RecommendedTrackRow {
  Long getTrackId();
  String getSpotifyId();
  String getTrackName();
  String getArtistName();
  String getAlbumName();
  String getImageUrl();
  Integer getDurationMs();
  Integer getRating();
}
