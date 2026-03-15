package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DailyTrackRepository extends JpaRepository<DailyTrack, Long> {

  Optional<DailyTrack> findByUser_idAndSelectedDate(Long userId, LocalDate selectedDate);

  List<DailyTrack> findByUser_idOrderBySelectedDate(Long userId);

  @Query("""
      select dt.selectedDate
      from DailyTrack dt
      where dt.user.id = :userId
      order by dt.selectedDate desc
      """)
  List<LocalDate> findSelectedDateByUser_idOrderBySelectedDateDesc(Long userId);

  @Query("""
      select dt
      from DailyTrack dt
      where dt.user.id = :userId
      and dt.selectedDate between :start and :end
      """)
  List<DailyTrack> findByMonth(Long userId, LocalDate start, LocalDate end);
  @Query("""
      select dt
      from DailyTrack dt
      where dt.selectedDate = :date
      """)
  List<DailyTrack> findAllByDate(LocalDate date);
}
