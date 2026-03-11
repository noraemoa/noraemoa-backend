package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.spotify.api.SpotifyApiClient;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyGetArtistResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse.TrackItem;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackLikeRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {

  private final TrackRepository trackRepository;
  private final RequestTrackRepository requestTrackRepository;
  private final SpotifyApiClient spotifyApiClient;
  private final TrackLikeRepository trackLikeRepository;


  @Transactional
  public Track createTrack(TrackCreateRequestDto trackCreateRequestDto) {
    Track track = Track.builder()
        .spotifyId(trackCreateRequestDto.getSpotifyId())
        .name(trackCreateRequestDto.getName())
        .artist(trackCreateRequestDto.getArtist())
        .album(trackCreateRequestDto.getAlbum())
        .imageUrl(trackCreateRequestDto.getImageUrl())
        .durationMs(trackCreateRequestDto.getDurationMs())
        .build();
    return trackRepository.findBySpotifyId(trackCreateRequestDto.getSpotifyId())
        .orElseGet(() -> trackRepository.save(track));
  }

  @Transactional(readOnly = true)
  public List<Track> getAllTracks() {
    return trackRepository.findAll();
  }

  @Transactional(readOnly = true)
  public Track getTrack(Long id) {

    return trackRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("해당 트랙을 찾을 수 없습니다."));
  }

  @Transactional
  public Track findOrCreateTrack(TrackCreateRequestDto dto) {
    return trackRepository.findBySpotifyId(dto.getSpotifyId())
        .orElseGet(() -> createTrack(dto));
  }

  @Transactional(readOnly = true)
  public SpotifyTrackResponseDto search(Long userId, String query) {
    SpotifySearchResponse spotifyTracks = spotifyApiClient.search(query);

    List<String> spotifyIds = spotifyTracks.tracks().items().stream()
        .map(SpotifySearchResponse.TrackItem::id).toList();
    Set<String> likedSpotifyIds = new HashSet<>(
        trackLikeRepository.findLikedSpotifyIds(userId, spotifyIds));

    return SpotifyTrackResponseDto.from(spotifyTracks, likedSpotifyIds);
  }

  @Transactional(readOnly = true)
  public SpotifyGetArtistResponse getArtist(String id) {
    return spotifyApiClient.getArtist(id);
  }
}
