package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.*;
import com.in28minutes.webservices.songrec.dto.request.PlaylistCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.*;
import com.in28minutes.webservices.songrec.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/playlists")
public class PlaylistController {
    private final PlaylistService playlistService;
    private final PlaylistTrackService playlistTrackService;

    // playlists
    @PostMapping
    public
    ResponseEntity<PlaylistResponseDto> createPlaylist(
            @Valid @RequestBody PlaylistCreateRequestDto playlistDto,
            @PathVariable @NotNull @Positive Long userId) {
        Playlist playlist = playlistService.createPlaylist(userId, playlistDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(PlaylistResponseDto.from(playlist));
    }

    @PatchMapping("/{playlistId}")
    public PlaylistResponseDto updatePlaylist(@Valid @RequestBody PlaylistCreateRequestDto playlistDto,
                                            @PathVariable @NotNull @Positive Long userId,
                                            @PathVariable @NotNull @Positive Long playlistId) {
        Playlist playlist = playlistService.updatePlaylist(playlistDto, userId, playlistId);
        return PlaylistResponseDto.from(playlist);
    }

    @GetMapping
    public List<PlaylistResponseDto> getPlaylists(@PathVariable @NotNull @Positive Long userId) {
        List<Playlist> playlists = playlistService.getPlaylistsByUserId(userId);

        return playlists.stream().map(PlaylistResponseDto::from).toList();
    }

    @GetMapping("/{playlistId}")
    public PlaylistResponseDto getPlaylistDetails(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId){

        Playlist playlist = playlistService.getActivePlaylist(userId,playlistId);
        return PlaylistResponseDto.from(playlist);
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylist(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId) {
        playlistService.deletePlaylist(userId, playlistId);
        return ResponseEntity.noContent().build();
    }

    // tracks
    @GetMapping("/{playlistId}/tracks")
    public List<TrackResponseDto> getTracksByPlaylist(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId) {
        List<Track> trackList = playlistTrackService.getTracksByPlaylist(userId,playlistId);
        return trackList
                .stream().map(TrackResponseDto::from)
                .toList();
    }

    @PostMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<PlaylistTrackResponseDto> addTrackByPlaylist(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId,
            @PathVariable @NotNull @Positive Long trackId) {
        PlaylistTrack pt=  playlistTrackService.addTrackByPlaylist(userId,playlistId,trackId);

        return ResponseEntity.status(HttpStatus.CREATED).body(PlaylistTrackResponseDto.from(pt));
    }

    @DeleteMapping("/{playlistId}/tracks/{trackId}")
    public ResponseEntity<Void> deleteTrackByPlaylist(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId,
            @PathVariable @NotNull @Positive Long trackId) {

        playlistTrackService.deleteTrack(userId,playlistId,trackId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/{playlistId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlaylistResponseDto> uploadThumbnail(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long playlistId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        Playlist playlist = playlistService.uploadThumbnail(userId,playlistId,file);
        return ResponseEntity.ok(PlaylistResponseDto.from(playlist));
    }
}