package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.request.RequestTrack;
import com.noraemoa.songrec.domain.request.RequestTrackRating;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.dto.response.request.RecommendedTrackResponseDto;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.RequestTrackRatingRepository;
import com.noraemoa.songrec.repository.RequestTrackRepository;
import com.noraemoa.songrec.repository.TrackLikeRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.repository.projection.RecommendedTrackRow;
import com.noraemoa.songrec.repository.projection.RequestTrackCountRow;
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
  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final RequestTrackRatingRepository requestTrackRatingRepository;
  private final TrackLikeRepository trackLikeRepository;
  private final RequestOwnershipService requestOwnershipService;

  @Transactional
  public RequestTrack getActiveRequestTrack(Long userId, Long requestId, Long trackId) {
    requestOwnershipService.getOwnedRequest(userId, requestId);
    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("RequestTrack not found"));
  }

  @Transactional(readOnly = true)
  public List<RecommendedTrackResponseDto> getTracksByRequest(Long userId, Long requestId) {
    List<RecommendedTrackRow> recommendedTracks = requestTrackRepository
        .findAllRecommendedTracksByRequestId(userId, requestId);

    if (recommendedTracks.isEmpty()) {
      return List.of();
    }

    List<String> trackIds = recommendedTracks.stream().map(RecommendedTrackRow::getSpotifyId)
        .toList();
    Set<String> likedSpotifyIds = new HashSet<>(
        trackLikeRepository.findLikedSpotifyIds(userId, trackIds));
    return recommendedTracks.stream().map(t -> RecommendedTrackResponseDto.from(t, likedSpotifyIds))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<RequestTrackCountRow> getTrackCountsByRequests(List<Long> requestIds) {

    return requestTrackRepository.countActiveTracksByRequestIds(requestIds);
  }

  // 본인 요청에만 곡을 추가할 수 있다. 추천 요청 생성(RequestWriteService.saveRequest)도 이 경로를 쓴다.
  @Transactional
  public RequestTrack addTrackByRequest(Long userId, Long requestId, Long trackId) {
    Request request = requestOwnershipService.getOwnedRequest(userId, requestId);
    Track track = trackService.getTrack(trackId);

    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId).map(existing -> {
      if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
        existing.setTrackDeleted(false);
      }
      return existing;
    }).orElseGet(() -> requestTrackRepository.save(
        RequestTrack.builder().request(request).track(track).trackDeleted(false).build()));
  }

  @Transactional
  public RequestTrack addSpotifyTrackToRequest(Long userId, Long requestId,
      TrackCreateRequestDto dto) {
    Request request = requestOwnershipService.getOwnedRequest(userId, requestId);
    Track track = trackService.findOrCreateTrack(dto);
    return requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, track.getId())
        .map(existing -> {
          if (Boolean.TRUE.equals(existing.getTrackDeleted())) {
            existing.setTrackDeleted(false);
          }
          return existing;
        }).orElseGet(() -> requestTrackRepository.save(
            RequestTrack.builder().request(request).track(track).trackDeleted(false).build()));
  }

  @Transactional
  public RequestTrackRating rateTrack(Long userId, Long requestId, Long trackId, Integer rating) {
    if (rating == null || rating < 1 || rating > 5) {
      throw new IllegalArgumentException("rating은 1~5");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("해당 요청에 트랙이 없습니다."));

    double avgRating = rt.getAvgRating() == null ? 0.0 : rt.getAvgRating();
    int ratingCount = rt.getRatingCount() == null ? 0 : rt.getRatingCount();

    RequestTrackRating existingRating = requestTrackRatingRepository.findByRequestTrack_IdAndUser_Id(
            rt.getId(), userId)
        .orElse(null);
    if(existingRating==null){
      double newAvg =(avgRating * ratingCount + rating) / (ratingCount + 1);
      rt.setAvgRating(newAvg);
      rt.setRatingCount(ratingCount + 1);

      RequestTrackRating requestTrackRating = RequestTrackRating.builder().requestTrack(rt).user(user)
          .rating(rating).build();
      return requestTrackRatingRepository.save(requestTrackRating);
    }

    int oldRating = existingRating.getRating();
    double newAvg =ratingCount==0?rating:(avgRating*ratingCount -oldRating +rating)/ratingCount;

    rt.setAvgRating(newAvg);
    rt.setRatingCount(ratingCount);

    existingRating.setRating(rating);
    return requestTrackRatingRepository.save(existingRating);
  }

  public RequestTrackRating getRequestTrackRating(Long userId, Long requestId, Long trackId) {
    RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("해당 요청에 트랙이 없습니다."));
    return requestTrackRatingRepository.findByRequestTrack_IdAndUser_Id(rt.getId(), userId)
        .orElseThrow(() -> new NotFoundException("해당 트랙에 대한 정보를 찾을 수 없습니다."));

  }

  @Transactional
  public void deleteTrack(Long userId, Long requestId, Long trackId) {
    requestOwnershipService.getOwnedRequest(userId, requestId);

    RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, trackId)
        .orElseThrow(() -> new NotFoundException("RequestTrack not found"));
    rt.setTrackDeleted(true);
  }
}
