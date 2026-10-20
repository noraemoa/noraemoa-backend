package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.dto.request.TrackSemanticSearchItemDto;
import com.noraemoa.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.noraemoa.songrec.dto.response.user.UserTasteProfileResponseDto;
import com.noraemoa.songrec.integration.openai.dto.UserTasteProfileResult;
import com.noraemoa.songrec.service.UserTasteOnboardingService;
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
