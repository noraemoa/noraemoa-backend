package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.LoginRequestDto;
import com.in28minutes.webservices.songrec.dto.response.LoginResponseDto;
import com.in28minutes.webservices.songrec.global.exception.UnauthorizedException;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public LoginResponseDto login(LoginRequestDto request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String token = jwtProvider.createAccessToken(user.getId(),user.getRole().name());

        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(token)
                .build();
    }
}
