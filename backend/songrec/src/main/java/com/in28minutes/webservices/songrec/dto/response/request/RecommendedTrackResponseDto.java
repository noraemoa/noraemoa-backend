package com.in28minutes.webservices.songrec.dto.response.request;

import com.in28minutes.webservices.songrec.repository.projection.RecommendedTrackRow;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class RecommendedTrackResponseDto {
  private Long trackId;
  private String spotifyId;
  private String trackName;
  private String artistName;
  private String albumName;
  private String imageUrl;
  private Integer durationMs;
  private Integer rating;
  private boolean liked;

  public  static RecommendedTrackResponseDto from(RecommendedTrackRow recommendedTrackRow,
      Set<String> likedIds) {
    return RecommendedTrackResponseDto.builder()
        .trackId(recommendedTrackRow.getTrackId())
        .spotifyId(recommendedTrackRow.getSpotifyId())
        .trackName(recommendedTrackRow.getTrackName())
        .artistName(recommendedTrackRow.getArtistName())
        .albumName(recommendedTrackRow.getAlbumName())
        .imageUrl(recommendedTrackRow.getImageUrl())
        .durationMs(recommendedTrackRow.getDurationMs())
        .rating(recommendedTrackRow.getRating()==null?0:recommendedTrackRow.getRating())
        .liked(likedIds.contains(recommendedTrackRow.getSpotifyId())).build();
  }
}
