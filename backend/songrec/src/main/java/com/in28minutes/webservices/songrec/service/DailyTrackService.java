package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.DailyTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyTrackService {

  private final EntityManager entityManager;
  private final TrackService trackService;
  private final DailyTrackRepository dailyTrackRepository;

  @Transactional
  public DailyTrack saveTodayTrack(Long userId, TrackCreateRequestDto dto) {
    LocalDate today = LocalDate.now();

    DailyTrack dailyTrack = dailyTrackRepository.findByUser_idAndSelectedDate(userId, today)
        .orElse(null);

    Track track = trackService.findOrCreateTrack(dto);
    if (dailyTrack == null) {
      User userRef = entityManager.getReference(User.class, userId);
      dailyTrack = DailyTrack.builder()
          .user(userRef)
          .track(track)
          .selectedDate(today)
          .build();
    } else {
      dailyTrack.setTrack(track);
    }
    return dailyTrackRepository.save(dailyTrack);
  }

  @Transactional(readOnly = true)
  public DailyTrack getTodayTrack(Long userId) {
    LocalDate today = LocalDate.now();

    return dailyTrackRepository.findByUser_idAndSelectedDate(userId, today)
        .orElseThrow(() -> new NotFoundException("오늘의 한 곡을 등록해보세요."));
  }

  @Transactional(readOnly = true)
  public List<DailyTrack> getMyDailyTracks(Long userId) {
    return dailyTrackRepository.findByUser_idOrderBySelectedDate(userId);
  }
}
