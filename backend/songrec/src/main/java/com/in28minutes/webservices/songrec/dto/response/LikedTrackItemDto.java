package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.repository.projection.LikedTrackRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class LikedTrackItemDto {

  private Long trackId;
  private String name;
  private String artist;
  private String album;
  private String imageUrl;
  private LocalDateTime createdAt;

  public static LikedTrackItemDto from( LikedTrackRow trackRow) {
    return LikedTrackItemDto.builder()
        .trackId(trackRow.getTrackId())
        .name(trackRow.getName())
        .artist(trackRow.getArtist())
        .album(trackRow.getAlbum())
        .imageUrl(trackRow.getImageUrl())
        .createdAt(trackRow.getCreatedAt())
        .build();
  }
}
