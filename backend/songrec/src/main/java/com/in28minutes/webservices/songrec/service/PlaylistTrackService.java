package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.track.TrackResponseDto;
import com.in28minutes.webservices.songrec.dto.response.track.TrackResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.repository.PlaylistTrackRepository;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackLikeRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistTrackService {

  private final PlaylistService playlistService;
  private final PlaylistTrackRepository playlistTrackRepository;
  private final TrackService trackService;
  private final RequestTrackRepository requestTrackRepository;
  private final TrackLikeRepository trackLikeRepository;

  @Transactional
  public PlaylistTrack getActivePlaylistTrack(Long playlistId, Long trackId) {
    playlistService.getAccessiblePlaylist(playlistId);
    return playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId, trackId)
        .orElseThrow(() -> new NotFoundException("PlaylistTrack not found"));
  }

  @Transactional(readOnly = true)
  public List<TrackResponseDto> getTracksByPlaylist(Long userId, Long playlistId) {
    List<Track> tracks = playlistTrackRepository.findActiveTracksByPlaylistId(playlistId);
    return getTracks(tracks,userId);
  }

  public List<TrackResponseDto> getTracks(List<Track> tracks,Long userId ){
    List<String> spotifyIds = tracks.stream()
        .map(Track::getSpotifyId).toList();
    Set<String> likedSpotifyIds = new HashSet<>(
        trackLikeRepository.findLikedSpotifyIds(userId, spotifyIds));
    return tracks.stream()
        .map(t -> TrackResponseDto.from(t, likedSpotifyIds.contains(t.getSpotifyId()))).toList();
  }


  @Transactional
  public PlaylistTrack addTrackByPlaylist(Long userId, Long playlistId, Long trackId) {
    Playlist playlist = playlistService.getOwnedPlaylist(userId, playlistId);
    Track track = trackService.getTrack(trackId);

    return playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId, trackId)
        .map(existing -> {
          if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
            existing.setTrackDeleted(false);
          }
          return existing;
        })
        .orElseGet(() -> playlistTrackRepository.save(
            PlaylistTrack.builder()
                .playlist(playlist)
                .track(track)
                .trackDeleted(false).build()));
  }

  @Transactional
  public PlaylistTrack addSpotifyTrackToPlaylist(Long userId, Long playlistId,
      TrackCreateRequestDto dto) {
    Playlist playlist = playlistService.getOwnedPlaylist(userId, playlistId);
    Track track = trackService.findOrCreateTrack(dto);

    return playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId, track.getId())
        .map(existing -> {
          if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
            existing.setTrackDeleted(false);
          }
          return existing;
        })
        .orElseGet(() -> playlistTrackRepository.save(
            PlaylistTrack.builder()
                .playlist(playlist)
                .track(track)
                .trackDeleted(false).build()));
  }

  @Transactional
  public void deleteTrack(Long userId, Long playlistId, Long trackId) {
    playlistService.getOwnedPlaylist(userId, playlistId); //userId 검증용

    PlaylistTrack pt = playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId, trackId)
        .orElseThrow(() -> new NotFoundException("PlaylistTrack not found"));
    pt.setTrackDeleted(true);
  }
}
