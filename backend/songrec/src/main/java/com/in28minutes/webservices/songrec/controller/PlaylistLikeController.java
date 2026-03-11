package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.domain.like.PlaylistLike;
import com.in28minutes.webservices.songrec.dto.response.LikedPlaylistItemDto;
import com.in28minutes.webservices.songrec.dto.response.PlaylistLikeStatusDto;
import com.in28minutes.webservices.songrec.service.PlaylistLikeService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class PlaylistLikeController {

  private final PlaylistLikeService playlistLikeService;

  @PostMapping("/playlists/{playlistId}/likes")
  public ResponseEntity<PlaylistLikeStatusDto> addPlaylistLike(
      @PathVariable @NotNull @Positive Long playlistId,
      @AuthenticationPrincipal JwtPrincipal principal
  ) {
    PlaylistLike playlistLike = playlistLikeService.addPlaylistLike(principal.userId(), playlistId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(PlaylistLikeStatusDto.from(playlistLike));
  }

  @DeleteMapping("/playlists/{playlistId}/likes")
  public ResponseEntity<Void> removePlaylistLike(
      @PathVariable @NotNull @Positive Long playlistId,
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    playlistLikeService.removePlaylistLike(principal.userId(), playlistId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/me/liked-playlists")
  public List<LikedPlaylistItemDto> getLikedPlaylists(
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    return playlistLikeService.getPlaylistLikes(principal.userId());
  }
}
