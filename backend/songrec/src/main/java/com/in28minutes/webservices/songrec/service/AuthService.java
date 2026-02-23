package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;
import com.in28minutes.webservices.songrec.dto.request.LoginRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.LoginResponseDto;
import com.in28minutes.webservices.songrec.global.exception.BadRequestException;
import com.in28minutes.webservices.songrec.global.exception.UnauthorizedException;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
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

    @Transactional
    public User signup(UserCreateRequestDto userDto) {
        if(userRepository.existsByEmail(userDto.getEmail())){
            throw new BadRequestException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);
        playlistService.createBasicPlaylists(saved.getId());
        return saved;
    }
}
