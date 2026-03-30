package com.in28minutes.webservices.songrec.integration.spotify.dto;

import com.in28minutes.webservices.songrec.domain.track.Track;
import jakarta.persistence.Column;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SpotifyTrackResponseDto {

  private List<TrackDetails> tracks;

  @Getter
  @Builder
  public static class TrackDetails {

    private SpotifyTrack track;
    private Artist artist;
  }

  @Getter
  @Builder
  public static class SpotifyTrack {

    private String spotifyId;
    private String name;
    private String artistName;
    private String album;
    private String imageUrl;
    private Integer durationMs;
    private boolean liked;
  }

  @Getter
  @Builder
  public static class Artist {
    private String artistId;
  }


  public static SpotifyTrackResponseDto from(SpotifySearchResponse searchResponse,
      Set<String> likedSpotifyIds) {
    var track = searchResponse.tracks().items().stream()
        .map(t -> TrackDetails.builder()
            .track(SpotifyTrack.builder()
                .spotifyId(t.id())
                .name(t.name())
                .artistName(t.artists().get(0).name())
                .album(t.album().name())
                .imageUrl(t.album().images().get(0).url())
                .durationMs(t.durationMs())
                .liked(likedSpotifyIds.contains(t.id()))
                .build())
            .artist(Artist.builder()
                .artistId(t.artists().get(0).id())
                .build())
            .build()).toList();

    return SpotifyTrackResponseDto.builder().tracks(track).build();
  }
}
