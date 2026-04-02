package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.dto.request.TrackSemanticSearchItemDto;
import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.user.UserTasteProfileResponseDto;
import com.in28minutes.webservices.songrec.integration.openai.dto.UserTasteProfileResult;
import com.in28minutes.webservices.songrec.service.UserTasteOnboardingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/me/taste-profile")
public class UserTasteProfileController {
  private final UserTasteOnboardingService userTasteOnboardingService;

  @PostMapping
  public ResponseEntity<UserTasteProfileResponseDto> saveTasteProfile(
      @RequestBody @Valid UserTasteProfileCreateRequestDto dto,
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    UserTasteProfileResult profileResult = userTasteOnboardingService.saveUserTasteProfile(principal.userId(),dto);
    List<TrackSemanticSearchItemDto> tracks =  userTasteOnboardingService.searchWelcomeSongs(
        principal.userId());
    UserTasteProfileResponseDto result = UserTasteProfileResponseDto.from(tracks,profileResult);
    return ResponseEntity.status(HttpStatus.OK).body(result);
  }
}
