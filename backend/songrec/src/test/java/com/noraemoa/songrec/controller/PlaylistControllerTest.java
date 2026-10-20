package com.noraemoa.songrec.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.config.OsivConfig;
import com.noraemoa.songrec.config.security.JwtProvider;
import com.noraemoa.songrec.domain.playlist.Playlist;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserRole;
import com.noraemoa.songrec.dto.request.PlaylistCreateRequestDto;
import com.noraemoa.songrec.dto.response.playlist.PlaylistResponseDto;
import com.noraemoa.songrec.fixture.UserFixture;
import com.noraemoa.songrec.global.exception.GlobalExceptionHandler;
import com.noraemoa.songrec.service.PlaylistService;
import com.noraemoa.songrec.service.PlaylistTrackService;
import com.noraemoa.songrec.service.TrackService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

// OsivConfig 는 EntityManagerFactory 가 필요한데 WebMvcTest 슬라이스엔 JPA 가 없어서 제외한다.
@WebMvcTest(
    controllers = PlaylistController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OsivConfig.class))
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
  private TrackService trackService;
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
