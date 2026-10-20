package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.like.TrackLike;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.dto.response.LikedTrackItemDto;
import com.noraemoa.songrec.global.exception.ConflictException;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.TrackLikeRepository;
import com.noraemoa.songrec.repository.TrackRepository;
import com.noraemoa.songrec.repository.projection.LikedTrackRow;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrackLikeService {

  private final UserService userService;
  private final TrackService trackService;
  private final TrackLikeRepository trackLikeRepository;
  private final EntityManager entityManager;
  private final TrackRepository trackRepository;

  @Transactional
  public TrackLike addTrackLike(Long userId, Long trackId) {
    Track track = trackService.getTrack(trackId);

    return createTrackLike(userId, track);
  }

  @Transactional
  public TrackLike addSpotifyTrackLike(Long userId, TrackCreateRequestDto dto) {
    Track track = trackService.findOrCreateTrack(dto);
    return createTrackLike(userId, track);
  }

  private TrackLike createTrackLike(Long userId, Track track) {
    if (trackLikeRepository.existsByUser_IdAndTrack_Id(userId, track.getId())) {
      return trackLikeRepository.findByUser_IdAndTrack_id(userId, track.getId()).orElse(null);
    }

    User userRef = entityManager.getReference(User.class, userId);
    TrackLike trackLike = TrackLike.builder()
        .user(userRef)
        .track(track)
        .build();
    return trackLikeRepository.save(trackLike);
  }

  @Transactional
  public void removeTrackLike(Long userId, String spotifyId) {
    Track track = trackRepository.findBySpotifyId(spotifyId)
        .orElseThrow(() -> new NotFoundException("해당 트랙을 찾을 수 없습니다."));
    trackLikeRepository.deleteByUser_IdAndTrack_Id(userId, track.getId());
  }

  @Transactional(readOnly = true)
  public List<LikedTrackItemDto> getTrackLikes(Long userId) {
    List<LikedTrackRow> trackRows = trackLikeRepository.findLikedTracks(userId);
    return trackRows.stream().map(LikedTrackItemDto::from).toList();
  }
}
