package com.in28minutes.webservices.songrec.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.config.security.JwtProvider;
import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.user.UserRole;
import com.in28minutes.webservices.songrec.dto.request.PlaylistCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.playlist.PlaylistResponseDto;
import com.in28minutes.webservices.songrec.fixture.UserFixture;
import com.in28minutes.webservices.songrec.global.exception.GlobalExceptionHandler;
import com.in28minutes.webservices.songrec.service.PlaylistService;
import com.in28minutes.webservices.songrec.service.PlaylistTrackService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PlaylistController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class PlaylistControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private PlaylistService playlistService;
  @MockBean
  private PlaylistTrackService playlistTrackService;
  @MockBean
  private JwtProvider jwtProvider;

  @Test
  void createPlaylist_success() throws Exception {
    Long userId = 1L;
    User user = UserFixture.userRef(userId);
    PlaylistCreateRequestDto dto = PlaylistCreateRequestDto.builder().title("testTitle").build();
    Playlist playlist = Playlist.builder().id(1L).user(user).title("testTitle").build();

    PlaylistResponseDto response = PlaylistResponseDto.builder()
        .id(1L)
        .userId(userId)
        .title(dto.getTitle()).build();
    JwtPrincipal principal = new JwtPrincipal(userId, UserRole.USER.name());

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
            principal, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    when(playlistService.createPlaylist(eq(principal.userId()), any(PlaylistCreateRequestDto.class)))
        .thenReturn(playlist);

    mockMvc.perform(post("/playlists")
            .with(request->{
              SecurityContextHolder.getContext().setAuthentication(authentication);
              return request;
            })
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.userId").value(response.getUserId()))
        .andExpect(jsonPath("$.title").value(response.getTitle()));
  }


}
