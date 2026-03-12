package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.domain.dailyTrack.DailyTrack;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.dailyTrack.DailyTrackResponseDto;
import com.in28minutes.webservices.songrec.service.DailyTrackService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class DailyTrackController {

  private final DailyTrackService dailyTrackService;

  @PutMapping("/daily-tracks/today")
  ResponseEntity<DailyTrackResponseDto> saveTodayTrack(
      @AuthenticationPrincipal JwtPrincipal principal,
      @Valid @RequestBody TrackCreateRequestDto dto
  ){
    DailyTrack dailyTrack = dailyTrackService.saveTodayTrack(principal.userId(),dto);
    return ResponseEntity.ok(DailyTrackResponseDto.from(dailyTrack));
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
}
