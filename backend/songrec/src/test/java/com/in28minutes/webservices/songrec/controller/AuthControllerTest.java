package com.in28minutes.webservices.songrec.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;
import com.in28minutes.webservices.songrec.dto.request.LoginRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.LoginResponseDto;
import com.in28minutes.webservices.songrec.global.exception.BadRequestException;
import com.in28minutes.webservices.songrec.global.exception.GlobalExceptionHandler;
import com.in28minutes.webservices.songrec.global.exception.UnauthorizedException;
import com.in28minutes.webservices.songrec.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private AuthService authService;
  @MockBean
  private JwtProvider jwtProvider;

  @Test
  void login_success() throws Exception {
    LoginRequestDto request = LoginRequestDto.builder()
        .email("abc@test.com")
        .password("testPassword").build();

    LoginResponseDto response = LoginResponseDto.builder()
        .username("testUser")
        .accessToken("access-token").build();

    when(authService.login(any(LoginRequestDto.class),any()))
        .thenReturn(response);

    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access-token"))
        .andExpect(jsonPath("$.username").value("testUser"));
  }

  @Test
  void login_whenUnauthorized_thenThrow401() throws Exception{
    LoginRequestDto request = LoginRequestDto.builder()
        .email("unauth@test.com")
        .password("testPassword").build();

    when(authService.login(any(),any()))
        .thenThrow(new UnauthorizedException("이메일 또는 비밀번호가 올바르지 않습니다."));

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void signup_success() throws Exception{
    UserCreateRequestDto dto = UserCreateRequestDto.builder()
        .email("abc@test.com")
        .username("testUser")
        .password("testPassword").build();

    User user = User.builder()
        .id(1L)
        .email("abc@test.com")
        .username("testUser")
        .role(UserRole.USER).build();
    when(authService.signup(any(UserCreateRequestDto.class))).thenReturn(user);

    mockMvc.perform(post("/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L))
        .andExpect(jsonPath("$.email").value("abc@test.com"))
        .andExpect(jsonPath("$.username").value("testUser"));
  }

  @Test
  void signup_whenBadRequest_thenThrow400() throws Exception{
    UserCreateRequestDto dto = UserCreateRequestDto.builder()
        .email("bad@test.com")
        .username("testUser")
        .password("testPassword").build();

    when(authService.signup(any())).thenThrow(new BadRequestException("이미 사용 중인 이메일입니다."));
    mockMvc.perform(post("/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isBadRequest());
  }
}
