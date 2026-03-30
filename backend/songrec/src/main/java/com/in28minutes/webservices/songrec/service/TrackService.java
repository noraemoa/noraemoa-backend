package com.in28minutes.webservices.songrec.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationInput;
import com.in28minutes.webservices.songrec.integration.openai.dto.TrackTagGenerationResult;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.SongPayload;
import com.in28minutes.webservices.songrec.integration.spotify.api.SpotifyApiClient;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyGetArtistResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.repository.TrackLikeRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import com.in28minutes.webservices.songrec.service.openai.TrackTagGenerationService;
import com.in28minutes.webservices.songrec.service.qdrant.SongVectorIndexService;
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
  private final SpotifyApiClient spotifyApiClient;
  private final TrackLikeRepository trackLikeRepository;
  private final ObjectMapper objectMapper;
  private final TrackTagGenerationService trackTagGenerationService;
  private final SongVectorIndexService songVectorIndexService;

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

  public void upsertTrackVector(TrackCreateRequestDto dto,Long trackId){
    TrackTagGenerationInput input = TrackTagGenerationInput.builder()
        .spotifyId(dto.getSpotifyId())
        .name(dto.getName())
        .artist(dto.getArtist())
        .albumName(dto.getAlbum())
        .durationMs(dto.getDurationMs()).build();

    TrackTagGenerationResult tagGenerationResult = trackTagGenerationService.generateTags(input);
    SongPayload payload = SongPayload.builder()
        .trackId(trackId)
        .spotifyTrackId(dto.getSpotifyId())
        .title(dto.getName())
        .artist(dto.getArtist())
        .mood_tags(tagGenerationResult.getMood_tags())
        .scene_tags(tagGenerationResult.getScene_tags())
        .texture_tags(tagGenerationResult.getTexture_tags())
        .short_description(tagGenerationResult.getShort_description()).build();

    songVectorIndexService.upsertSong(payload);
  }

  @Transactional
  public void ensureTrackIndexed(Track track,TrackCreateRequestDto dto){
    if(Boolean.TRUE.equals(track.getVectorIndexed())){return;}
    upsertTrackVector(dto,track.getId());
    track.setVectorIndexed(true);
  }

  @Transactional
  public Track findOrCreateTrack(TrackCreateRequestDto dto) {

    return trackRepository.findBySpotifyId(dto.getSpotifyId())
        .orElseGet(() -> createTrack(dto));
  }

  @Transactional(readOnly = true)
  public SpotifyTrackResponseDto search(Long userId, String query) {
    SpotifySearchResponse spotifyTracks = spotifyApiClient.search(query);
    try {
      System.out.println(
          objectMapper.writerWithDefaultPrettyPrinter()
              .writeValueAsString(spotifyTracks)
      );
    } catch (Exception e) {
      e.printStackTrace();
    }
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
