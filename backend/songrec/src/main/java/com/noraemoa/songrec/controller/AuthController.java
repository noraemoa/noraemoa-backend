package com.noraemoa.songrec.controller;


import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.request.LoginRequestDto;
import com.noraemoa.songrec.dto.request.UserCreateRequestDto;
import com.noraemoa.songrec.dto.response.LoginResponseDto;
import com.noraemoa.songrec.dto.response.user.UserResponseDto;
import com.noraemoa.songrec.service.AuthService;
import com.noraemoa.songrec.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDto login(
            @Valid @RequestBody LoginRequestDto requestDto,
            HttpServletResponse response
    ){
        return authService.login(requestDto,response);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(
            @Valid @RequestBody UserCreateRequestDto userDto) {
        User user = authService.signup(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.from(user));
    }

    @PostMapping("/refresh")
    public LoginResponseDto refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        return authService.refresh(request,response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        authService.logout(request,response);
        return ResponseEntity.noContent().build();
    }
}
