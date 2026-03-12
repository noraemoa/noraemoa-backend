package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTrackRepository extends JpaRepository<DailyTrack, Long> {

  Optional<DailyTrack> findByUser_idAndSelectedDate(Long userId, LocalDate selectedDate);
  List<DailyTrack> findByUser_idOrderBySelectedDate(Long userId);
}
