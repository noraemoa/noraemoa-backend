package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.PlaylistCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.PlaylistVisibilityRequestDto;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.*;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/playlists")
public class PlaylistController {

  private final PlaylistService playlistService;
  private final PlaylistTrackService playlistTrackService;

  // playlists
  @PostMapping
  public ResponseEntity<PlaylistResponseDto> createPlaylist(
      @Valid @RequestBody PlaylistCreateRequestDto playlistDto,
      @AuthenticationPrincipal JwtPrincipal principal) {
    Playlist playlist = playlistService.createPlaylist(principal.userId(), playlistDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(PlaylistResponseDto.from(playlist));
  }

  @PatchMapping("/{playlistId}")
  public PlaylistResponseDto updatePlaylist(
      @Valid @RequestBody PlaylistCreateRequestDto playlistDto,
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId) {
    Playlist playlist = playlistService.updatePlaylist(playlistDto, principal.userId(), playlistId);
    return PlaylistResponseDto.from(playlist);
  }

  @GetMapping
  public List<PlaylistResponseDto> getMyPlaylists(@AuthenticationPrincipal JwtPrincipal principal) {
    List<Playlist> playlists = playlistService.getPlaylistsByUserId(principal.userId());

    return playlists.stream().map(PlaylistResponseDto::from).toList();
  }

  @GetMapping("/{playlistId}")
  public PlaylistResponseDto getPlaylistDetails(
      @PathVariable @NotNull @Positive Long playlistId) {

    Playlist playlist = playlistService.getAccessiblePlaylist(playlistId);
    return PlaylistResponseDto.from(playlist);
  }

  @PatchMapping("/{playlistId}/visibility")
  public PlaylistResponseDto updateVisibility(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId,
      @RequestBody @Valid PlaylistVisibilityRequestDto dto) {
    Playlist playlist = playlistService.updateVisibility(principal.userId(), playlistId,
        dto.getVisibility());
    return PlaylistResponseDto.from(playlist);
  }

  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> deletePlaylist(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId) {
    playlistService.deletePlaylist(principal.userId(), playlistId);
    return ResponseEntity.noContent().build();
  }

  // tracks
  @GetMapping("/{playlistId}/tracks")
  public List<TrackResponseDto> getTracksByPlaylist(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId) {
    return playlistTrackService.getTracksByPlaylist(principal.userId(), playlistId);
  }

  // track에 추가되지 않은 spotify track을 track테이블에 먼저 추가하고 플리에 해당 track 저장
  @PostMapping("/{playlistId}/tracks")
  public ResponseEntity<PlaylistTrackResponseDto> addSpotifyTrackByPlaylist(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId,
      @RequestBody @Valid TrackCreateRequestDto dto) {
    PlaylistTrack pt = playlistTrackService.addSpotifyTrackToPlaylist(principal.userId(),
        playlistId, dto);

    return ResponseEntity.status(HttpStatus.CREATED).body(PlaylistTrackResponseDto.from(pt));
  }

  // track 테이블에 있는 노래만 이 api 쓸 수 있음.
  @PostMapping("/{playlistId}/tracks/{trackId}")
  public ResponseEntity<PlaylistTrackResponseDto> addTrackByPlaylist(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId,
      @PathVariable @NotNull @Positive Long trackId) {
    PlaylistTrack pt = playlistTrackService.addTrackByPlaylist(principal.userId(), playlistId,
        trackId);

    return ResponseEntity.status(HttpStatus.CREATED).body(PlaylistTrackResponseDto.from(pt));
  }

  @DeleteMapping("/{playlistId}/tracks/{trackId}")
  public ResponseEntity<Void> deleteTrackByPlaylist(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId,
      @PathVariable @NotNull @Positive Long trackId) {

    playlistTrackService.deleteTrack(principal.userId(), playlistId, trackId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{playlistId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<PlaylistResponseDto> uploadThumbnail(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long playlistId,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    Playlist playlist = playlistService.uploadThumbnail(principal.userId(), playlistId, file);
    return ResponseEntity.ok(PlaylistResponseDto.from(playlist));
  }
}