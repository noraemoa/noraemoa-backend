package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.domain.dailyTrack.DailyTrack;
import com.noraemoa.songrec.domain.dailyTrack.DailyTrackEmotion;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.dto.response.dailyTrack.DailyTrackResponseDto;
import com.noraemoa.songrec.dto.response.dailyTrack.DailyTrackStreakResponseDto;
import com.noraemoa.songrec.dto.response.dailyTrack.YesterdayDailyTrackResponseDto;
import com.noraemoa.songrec.service.DailyTrackService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class DailyTrackController {

  private final DailyTrackService dailyTrackService;

  @PutMapping("/daily-tracks/today")
  public ResponseEntity<DailyTrackResponseDto> saveTodayTrack(
      @AuthenticationPrincipal JwtPrincipal principal,
      @Valid @RequestBody TrackCreateRequestDto dto
  ){
    DailyTrack dailyTrack = dailyTrackService.saveTodayTrack(principal.userId(),dto);
    return ResponseEntity.ok(DailyTrackResponseDto.from(dailyTrack));
  }
  @PutMapping("/daily-tracks/today/emotion")
  public ResponseEntity<DailyTrackResponseDto> saveTodayEmotion(
      @AuthenticationPrincipal JwtPrincipal principal,
      @RequestParam DailyTrackEmotion emotion
  ){
    DailyTrack dailyTrack = dailyTrackService.updateTodayEmotion(principal.userId(),emotion);
    return ResponseEntity.ok(DailyTrackResponseDto.from(dailyTrack));
  }

  @GetMapping("/users/me/daily-tracks/streak")
  public DailyTrackStreakResponseDto getCurrentStreak(@AuthenticationPrincipal JwtPrincipal principal){
    return dailyTrackService.getStreak(principal.userId());
  }


  @GetMapping("/daily-tracks/today")
  public DailyTrackResponseDto getTodayTrack(
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    DailyTrack dailyTrack = dailyTrackService.getTodayTrack(principal.userId());
    return DailyTrackResponseDto.from(dailyTrack);
  }

  @GetMapping("/users/me/daily-tracks")
  public List<DailyTrackResponseDto> getMyDailyTracks(
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    List<DailyTrack> dailyTracks=dailyTrackService.getMyDailyTracks(principal.userId());
    return dailyTracks.stream().map(DailyTrackResponseDto::from).toList();
  }

  @GetMapping("/users/me/daily-tracks/calendar")
  public List<DailyTrackResponseDto> getMonthlyDailyTracks(
      @AuthenticationPrincipal JwtPrincipal principal,
      @RequestParam int year,
      @RequestParam Month month
  ){
    List<DailyTrack> dailyTracks = dailyTrackService.getMonthTracks(principal.userId(),year,month);
    return dailyTracks.stream().map(DailyTrackResponseDto::from).toList();
  }

  @GetMapping("/daily-tracks/yesterday")
  public List<YesterdayDailyTrackResponseDto> getYesterdayDailyTracks(
  ){
    List<DailyTrack> dailyTracks = dailyTrackService.getYesterdayTracks();
    return dailyTracks.stream().map(YesterdayDailyTrackResponseDto::from).toList();
  }
}
