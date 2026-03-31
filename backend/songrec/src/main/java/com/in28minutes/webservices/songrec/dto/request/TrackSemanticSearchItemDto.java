package com.in28minutes.webservices.songrec.dto.request;

import com.in28minutes.webservices.songrec.domain.track.Track;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackSemanticSearchItemDto {
  private Long trackId;
  private String spotifyId;
  private String name;
  private String artist;
  private String album;
  private String imageUrl;
  private Integer durationMs;
  private Double score;

  public static TrackSemanticSearchItemDto from(Track track, Double score) {
    return TrackSemanticSearchItemDto.builder()
        .trackId(track.getId())
        .spotifyId(track.getSpotifyId())
        .name(track.getName())
        .artist(track.getArtist())
        .album(track.getAlbum())
        .imageUrl(track.getImageUrl())
        .durationMs(track.getDurationMs())
        .score(score)
        .build();
  }
}
