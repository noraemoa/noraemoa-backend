package com.in28minutes.webservices.songrec.dto.response.playlist;

import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import com.in28minutes.webservices.songrec.repository.projection.PopularPlaylistRow;
import lombok.Builder;
import lombok.Getter;

@Builder @Getter
public class PopularPlaylistResponseDto {
  private Long id;
  private Long userId;
  private String username;
  private String title;
  private String thumbnailUrl;
  private PlaylistVisibility visibility;
  private Integer likeCount;

  public static PopularPlaylistResponseDto from(PopularPlaylistRow playlistRow){
    return PopularPlaylistResponseDto.builder()
        .id(playlistRow.getPlaylistId())
        .userId(playlistRow.getUserId())
        .username(playlistRow.getUsername())
        .title(playlistRow.getTitle())
        .thumbnailUrl(playlistRow.getThumbnailUrl())
        .visibility(playlistRow.getVisibility())
        .likeCount(playlistRow.getLikeCount()).build();
  }

}
