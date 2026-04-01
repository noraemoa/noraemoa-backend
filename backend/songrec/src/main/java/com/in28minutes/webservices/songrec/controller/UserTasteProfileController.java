package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.service.UserTasteOnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
  public ResponseEntity<Void> saveTasteProfile(
      @RequestBody @Valid UserTasteProfileCreateRequestDto dto,
      @AuthenticationPrincipal JwtPrincipal principal
  ){
    userTasteOnboardingService.saveUserTasteProfile(principal.userId(),dto);
    return ResponseEntity.ok().build();
  }
}
