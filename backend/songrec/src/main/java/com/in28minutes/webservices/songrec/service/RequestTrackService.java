package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.request.RequestTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.TrackResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto.SpotifyTrack;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackLikeRepository;
import com.in28minutes.webservices.songrec.repository.projection.RequestTrackCountRow;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTrackService {

  private final RequestTrackRepository requestTrackRepository;
  private final TrackService trackService;
  private final RequestService requestService;
  private final TrackLikeRepository trackLikeRepository;
  private final PlaylistTrackService playlistTrackService;

  @Transactional
  public RequestTrack getActiveRequestTrack(Long userId, Long requestId, Long trackId) {
    requestService.getActiveRequest(userId, requestId);
    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("RequestTrack not found"));
  }

  @Transactional(readOnly = true)
  public List<TrackResponseDto> getTracksByRequest(Long userId,Long requestId) {

    List<Track> tracks= requestTrackRepository.findActiveTracksByRequestId(requestId);
    return playlistTrackService.getTracks(tracks,userId);
    }

  @Transactional(readOnly = true)
  public List<RequestTrackCountRow> getTrackCountsByRequests(List<Long> requestIds) {

    return requestTrackRepository.countActiveTracksByRequestIds(requestIds);
  }

  @Transactional
  public RequestTrack addTrackByRequest(Long requestId, Long trackId) {
    Request request = requestService.getRequestFeed(requestId);
    Track track = trackService.getTrack(trackId);

    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .map(existing -> {
          if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
            existing.setTrackDeleted(false);
          }
          return existing;
        })
        .orElseGet(() -> requestTrackRepository.save(
            RequestTrack.builder()
                .request(request)
                .track(track)
                .trackDeleted(false).build()));
  }

  @Transactional
  public RequestTrack addSpotifyTrackToRequest( Long requestId,
      TrackCreateRequestDto dto) {
    Request request = requestService.getRequestFeed(requestId);
    Track track = trackService.findOrCreateTrack(dto);
    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, track.getId())
        .map(existing -> {
          if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
            existing.setTrackDeleted(false);
          }
          return existing;
        })
        .orElseGet(() -> requestTrackRepository.save(
            RequestTrack.builder()
                .request(request)
                .track(track)
                .trackDeleted(false).build()));
  }

  @Transactional
  public RequestTrack rateTrack(Long userId, Long requestId, Long trackId, Integer rating) {
    requestService.getActiveRequest(userId, requestId);
    RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("해당 요청에 트랙이 없습니다."));
    if (rating < 1 || rating > 5) {
      throw new IllegalArgumentException("rating은 1~5");
    }
    rt.setRating(rating);
    return rt;
  }

  @Transactional
  public void deleteTrack(Long userId, Long requestId, Long trackId) {
    requestService.getActiveRequest(userId, requestId); //userId 검증용

    RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("RequestTrack not found"));
    rt.setTrackDeleted(true);
  }
}
