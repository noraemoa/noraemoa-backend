package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.RefreshToken;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;
import com.in28minutes.webservices.songrec.dto.request.LoginRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.LoginResponseDto;
import com.in28minutes.webservices.songrec.global.exception.BadRequestException;
import com.in28minutes.webservices.songrec.global.exception.UnauthorizedException;
import com.in28minutes.webservices.songrec.repository.RefreshTokenRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Ref;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlaylistService playlistService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private void addRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken",token)
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(14))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteRefreshCookie(HttpServletResponse response){
        ResponseCookie cookie = ResponseCookie.from("refreshToken","")
                .httpOnly(true)
                .secure(false)
                .path("/auth")
                .sameSite("Lax")
                .maxAge(0)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Transactional
    public LoginResponseDto login(LoginRequestDto request,
                                  HttpServletResponse response){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(),user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .tokenHash(tokenHash)
                        .user(user)
                        .expiresAt(LocalDateTime.now().plusDays(14))
                        .build()
        );

        addRefreshCookie(response, refreshToken);

        return LoginResponseDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole().name())
                .accessToken(accessToken)
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

    private String extractRefreshToken(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new UnauthorizedException("Refresh token not found");
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new UnauthorizedException("Refresh token not found");
    }

    //새 access token 발급
    @Transactional
    public LoginResponseDto refresh(HttpServletRequest request,
                                    HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);

        Claims claims = jwtProvider.parseToken(refreshToken);
        String type = claims.get("type",String.class);
        if(!"refresh".equals(type)){
            throw new UnauthorizedException("Invalid token type");
        }

        Long userId = Long.valueOf(claims.getSubject());

        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        RefreshToken saved = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(()-> new UnauthorizedException("Invalid refresh token"));

        if(saved.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new UnauthorizedException("Refresh token expired");
        }

        // 기존 refresh 토큰 삭제, 새로 만듦
        saved.setRevoked(true);

        String newRefresh = jwtProvider.createRefreshToken(userId);
        String newHash = DigestUtils.sha256Hex(newRefresh);

        refreshTokenRepository.save(
                RefreshToken.builder()
                        .tokenHash(newHash)
                        .user(saved.getUser())
                        .expiresAt(LocalDateTime.now().plusDays(14))
                        .build()
        );

        addRefreshCookie(response,newRefresh);

        String newAccess = jwtProvider.createAccessToken(userId,
                saved.getUser().getRole().name());

        return LoginResponseDto.builder()
                .accessToken(newAccess)
                .userId(userId)
                .email(saved.getUser().getEmail())
                .username(saved.getUser().getUsername())
                .role(saved.getUser().getRole().name())
                .build();
    }

    @Transactional
    public void logout(HttpServletRequest request,
                       HttpServletResponse response) {
        String refreshToken = extractRefreshToken(request);
        String tokenHash = DigestUtils.sha256Hex(refreshToken);

        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(token -> token.setRevoked(true));

        deleteRefreshCookie(response);
    }
}
