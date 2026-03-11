package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.track.Track;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackSimpleResponseDto {
  private Long trackId;
  private String spotifyId;
  private String name;
  private String artist;
  private String album;
  private String imageUrl;
  private Integer durationMs;

  public static TrackSimpleResponseDto from(Track track) {
    return TrackSimpleResponseDto.builder()
        .trackId(track.getId())
        .spotifyId(track.getSpotifyId())
        .name(track.getName())
        .artist(track.getArtist())
        .album(track.getAlbum())
        .imageUrl(track.getImageUrl())
        .durationMs(track.getDurationMs()).build();
  }
}
