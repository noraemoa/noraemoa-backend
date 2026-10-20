package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.dto.response.TrackSimpleResponseDto;
import com.noraemoa.songrec.integration.spotify.dto.SpotifyArtistResponseDto;
import com.noraemoa.songrec.integration.spotify.dto.SpotifyGetArtistResponse;
import com.noraemoa.songrec.integration.spotify.dto.SpotifySearchResponse;
import com.noraemoa.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.noraemoa.songrec.service.TrackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class TrackController {

  private final TrackService trackService;

  public TrackController(TrackService trackService) {
    this.trackService = trackService;
  }

  @PostMapping("/tracks")
  public ResponseEntity<TrackSimpleResponseDto> createTrack(
      @Valid @RequestBody TrackCreateRequestDto trackCreateRequestDto) {

    Track track = trackService.createTrack(trackCreateRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(TrackSimpleResponseDto.from(track));
  }

  @GetMapping("/tracks")
  public List<TrackSimpleResponseDto> GetTrack() {

    List<Track> trackList = trackService.getAllTracks();
    return trackList.stream().map(TrackSimpleResponseDto::from).toList();
  }

  @GetMapping(value = "/tracks/search", produces = "application/json")
  public SpotifyTrackResponseDto search(@AuthenticationPrincipal JwtPrincipal principal,
      @RequestParam String q) {
    return trackService.search(principal.userId(),q);
  }

  @GetMapping("/tracks/artist/{id}")
  public SpotifyArtistResponseDto getArtist(@PathVariable String id) {
    SpotifyGetArtistResponse res = trackService.getArtist(id);
    return SpotifyArtistResponseDto.from(res);
  }
}
