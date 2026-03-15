package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrackEmotion;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.dailyTrack.DailyTrackStreakResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.DailyTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
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

    Track track = trackService.findOrCreateTrack(
        TrackCreateRequestDto.builder().spotifyId(dto.getSpotifyId()).name(dto.getName())
            .artist(dto.getArtist()).album(dto.getAlbum()).imageUrl(dto.getImageUrl())
            .durationMs(dto.getDurationMs()).build());
    if (dailyTrack == null) {
      User userRef = entityManager.getReference(User.class, userId);
      dailyTrack = DailyTrack.builder().user(userRef).track(track).selectedDate(today).build();
    } else {
      dailyTrack.setTrack(track);
    }
    return dailyTrackRepository.save(dailyTrack);
  }

  @Transactional
  public DailyTrack updateTodayEmotion(Long userId, DailyTrackEmotion emotion) {
    LocalDate today = LocalDate.now();

    DailyTrack dailyTrack = dailyTrackRepository.findByUser_idAndSelectedDate(userId, today)
        .orElseThrow(() -> new NotFoundException("오늘의 한 곡을 등록하지 않았습니다."));

    dailyTrack.setEmotion(emotion);
    return dailyTrackRepository.save(dailyTrack);
  }

  @Transactional(readOnly = true)
  public DailyTrackStreakResponseDto getStreak(Long userId) {
    List<LocalDate> dates = dailyTrackRepository.findSelectedDateByUser_idOrderBySelectedDateDesc(
        userId);

    if (dates.isEmpty()) {
      return DailyTrackStreakResponseDto.builder().currentStreak(0).build();
    }

    LocalDate today = LocalDate.now();
    int currentStreak = 0;

    if (!dates.contains(today)) {
      return DailyTrackStreakResponseDto.builder().currentStreak(0).build();
    }

    LocalDate cursor = today;
    currentStreak = 1;

    for (LocalDate date : dates) {
      if (date.equals(today)) {
        continue;
      }

      if (date.equals(cursor.minusDays(1))) {
        currentStreak++;
        cursor = date;
      } else if (date.isBefore(cursor.minusDays(1))) {
        break;
      }
    }

    return DailyTrackStreakResponseDto.builder().currentStreak(currentStreak).build();
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

  @Transactional(readOnly = true)
  public List<DailyTrack> getMonthTracks(Long userId, int year, Month month) {
    YearMonth ym = YearMonth.of(year, month);
    LocalDate start = ym.atDay(1);
    LocalDate end = ym.atEndOfMonth();
    return dailyTrackRepository.findByMonth(userId, start, end);
  }

  @Transactional(readOnly = true)
  public List<DailyTrack> getYesterdayTracks() {
    LocalDate yesterday = LocalDate.now().minusDays(1);

    return dailyTrackRepository.findAllByDate(yesterday);
  }
}
