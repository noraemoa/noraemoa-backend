package com.in28minutes.webservices.songrec.dto.response.dailyTrack;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class DailyTrackStreakResponseDto {
  private Integer currentStreak;
}
