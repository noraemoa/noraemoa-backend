package com.in28minutes.webservices.songrec.controller;


import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.LoginRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.LoginResponseDto;
import com.in28minutes.webservices.songrec.dto.response.UserResponseDto;
import com.in28minutes.webservices.songrec.service.AuthService;
import com.in28minutes.webservices.songrec.service.UserService;
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

    private final UserService userService;
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
