package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.domain.like.TrackLike;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.dto.response.LikedTrackItemDto;
import com.noraemoa.songrec.dto.response.TrackLikeStatusDto;
import com.noraemoa.songrec.service.TrackLikeService;
import com.noraemoa.songrec.service.TrackService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class TrackLikeController {

  private final TrackLikeService trackLikeService;
  private final TrackService trackService;

  @PostMapping("/tracks/{trackId}/likes")
  public ResponseEntity<TrackLikeStatusDto> addTrackLike(
      @PathVariable @NotNull @Positive Long trackId,
      @AuthenticationPrincipal JwtPrincipal principal
  ) {
    TrackLike trackLike = trackLikeService.addTrackLike(principal.userId(), trackId);
    return ResponseEntity.status(HttpStatus.CREATED).body(TrackLikeStatusDto.from(trackLike));
  }

  @PostMapping("/tracks/likes")
  public ResponseEntity<TrackLikeStatusDto> addSpotifyTrackLike(
      @RequestBody @Valid TrackCreateRequestDto dto,
      @AuthenticationPrincipal JwtPrincipal principal
  ) {
    TrackLike trackLike = trackLikeService.addSpotifyTrackLike(principal.userId(), dto);

    try{
      trackService.ensureTrackIndexed(trackLike.getTrack(),dto);
    }catch (Exception e){
      e.printStackTrace();
    }


    return ResponseEntity.status(HttpStatus.CREATED).body(TrackLikeStatusDto.from(trackLike));
  }

  @DeleteMapping("/tracks/{spotifyId}/likes")
  public ResponseEntity<Void> removeTrackLike(
      @PathVariable String spotifyId,
      @AuthenticationPrincipal JwtPrincipal principal
  ) {
    trackLikeService.removeTrackLike(principal.userId(), spotifyId);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/users/me/liked-tracks")
  public List<LikedTrackItemDto> getLikedTracks(
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    return trackLikeService.getTrackLikes(principal.userId());
  }

}
