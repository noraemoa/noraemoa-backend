package com.in28minutes.webservices.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.BadRequestException;
import com.in28minutes.webservices.songrec.repository.RefreshTokenRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private PlaylistService playlistService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Test
  void signup_success() {
    UserCreateRequestDto dto = UserCreateRequestDto.builder()
        .email("abc@test.com")
        .username("testUser")
        .password("testPassword").build();

    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

    doAnswer(invocation-> {
          User user = invocation.getArgument(0);
          user.setId(1L);
          return user;
        }
    ).when(userRepository).save(any(User.class));

    User response = authService.signup(dto);
    verify(playlistService,times(1)).createBasicPlaylists(response.getId());

    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getEmail()).isEqualTo(dto.getEmail());
    assertThat(response.getPassword()).isEqualTo("encodedPassword");
    assertThat(response.getRole()).isEqualTo(UserRole.USER);
  }

  @Test
  void signup_whenAlreadyExists_thenThrowException(){
    UserCreateRequestDto dto = UserCreateRequestDto.builder()
        .email("abc@test.com")
        .username("testUser")
        .password("testPassword").build();
    when(userRepository.existsByEmail(dto.getEmail())).thenReturn(true);

    assertThatThrownBy(() -> authService.signup(dto))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("이미 사용 중인 이메일입니다.");
  }
}
