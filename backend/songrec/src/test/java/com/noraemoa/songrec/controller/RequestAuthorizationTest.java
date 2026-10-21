package com.noraemoa.songrec.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noraemoa.songrec.config.QdrantInitializer;
import com.noraemoa.songrec.config.security.JwtPrincipal;
import com.noraemoa.songrec.domain.keyword.Keyword;
import com.noraemoa.songrec.domain.playlist.Playlist;
import com.noraemoa.songrec.domain.playlist.PlaylistVisibility;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.request.RequestKeyword;
import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.domain.user.UserRole;
import com.noraemoa.songrec.dto.request.TrackCreateRequestDto;
import com.noraemoa.songrec.repository.KeywordRepository;
import com.noraemoa.songrec.repository.PlaylistRepository;
import com.noraemoa.songrec.repository.RequestKeywordRepository;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.RequestTrackRepository;
import com.noraemoa.songrec.repository.TrackRepository;
import com.noraemoa.songrec.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * 추천 요청/플레이리스트 API 의 권한 검증 테스트.
 *
 * 서비스와 DB(H2)를 실제로 실행해서, 권한이 없는 호출이 거절될 뿐 아니라
 * DB 데이터도 바뀌지 않았는지까지 확인한다. 보안 필터도 실제로 거친다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestAuthorizationTest {

  // 기동 시 Qdrant 에 컬렉션 생성 요청을 보내므로 외부 서버 없이 뜨도록 대체한다.
  @MockBean
  private QdrantInitializer qdrantInitializer;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private RequestRepository requestRepository;
  @Autowired
  private TrackRepository trackRepository;
  @Autowired
  private KeywordRepository keywordRepository;
  @Autowired
  private RequestTrackRepository requestTrackRepository;
  @Autowired
  private RequestKeywordRepository requestKeywordRepository;
  @Autowired
  private PlaylistRepository playlistRepository;

  private String unique;
  private User owner;
  private User other;
  private User admin;
  private Long requestId;
  private Track track;
  private Keyword keyword;

  @BeforeEach
  void setUp() {
    unique = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    owner = saveUser("owner", UserRole.USER);
    other = saveUser("other", UserRole.USER);
    admin = saveUser("admin", UserRole.ADMIN);
    requestId = requestRepository.save(Request.builder()
        .user(owner).originalPrompt("비 오는 날").title("비 오는 날").deleted(false).build()).getId();
    track = trackRepository.save(Track.builder()
        .spotifyId("sp" + unique).name("Rain").vectorIndexed(true).build());
    keyword = keywordRepository.save(Keyword.builder()
        .rawText("kw" + unique).normalizedText("kw" + unique).build());
  }

  // ---------- 관리자 API ----------

  @Test
  @DisplayName("일반 사용자가 관리자 삭제 API 를 호출하면 403 이고 요청은 삭제되지 않는다")
  void adminDelete_byUser_isForbidden() throws Exception {
    mockMvc.perform(delete("/requests/admin/{requestId}", requestId).with(as(other)))
        .andExpect(status().isForbidden())
        // @PreAuthorize 거부는 GlobalExceptionHandler.handleAccessDenied 가 응답한다.
        .andExpect(jsonPath("$.error").value("FORBIDDEN"));

    assertThat(requestRepository.findById(requestId).orElseThrow().getDeleted()).isFalse();
  }

  @Test
  @DisplayName("요청 소유자라도 일반 사용자는 관리자 삭제 API 를 쓸 수 없다(403)")
  void adminDelete_byOwnerWithUserRole_isForbidden() throws Exception {
    mockMvc.perform(delete("/requests/admin/{requestId}", requestId).with(as(owner)))
        .andExpect(status().isForbidden());

    assertThat(requestRepository.findById(requestId).orElseThrow().getDeleted()).isFalse();
  }

  @Test
  @DisplayName("관리자는 다른 사용자의 요청을 삭제할 수 있다(204)")
  void adminDelete_byAdmin_isNoContent() throws Exception {
    mockMvc.perform(delete("/requests/admin/{requestId}", requestId).with(as(admin)))
        .andExpect(status().isNoContent());

    assertThat(requestRepository.findById(requestId).orElseThrow().getDeleted()).isTrue();
  }

  // ---------- 요청 소유권: 다른 사용자 → 404, 데이터 변경 없음 ----------

  @Test
  @DisplayName("다른 사용자의 요청에 Spotify 곡을 추가하면 404 이고 곡도 연결도 만들어지지 않는다")
  void addSpotifyTrack_toOthersRequest_isNotFound() throws Exception {
    String newSpotifyId = "new" + unique;

    mockMvc.perform(post("/requests/{requestId}/tracks", requestId).with(as(other))
            .contentType(MediaType.APPLICATION_JSON)
            .content(trackJson(newSpotifyId)))
        .andExpect(status().isNotFound());

    assertThat(trackRepository.findBySpotifyId(newSpotifyId)).isEmpty();
  }

  @Test
  @DisplayName("다른 사용자의 요청에 곡을 추가하면 404 이고 연결이 만들어지지 않는다")
  void addTrack_toOthersRequest_isNotFound() throws Exception {
    mockMvc.perform(post("/requests/{requestId}/tracks/{trackId}", requestId, track.getId())
            .with(as(other)))
        .andExpect(status().isNotFound());

    assertThat(requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, track.getId()))
        .isEmpty();
  }

  @Test
  @DisplayName("다른 사용자의 요청에 키워드를 추가하면 404 이고 연결이 만들어지지 않는다")
  void addKeyword_toOthersRequest_isNotFound() throws Exception {
    mockMvc.perform(post("/requests/{requestId}/keywords/{keywordId}", requestId, keyword.getId())
            .with(as(other)))
        .andExpect(status().isNotFound());

    assertThat(requestKeywordRepository.findByRequest_IdAndKeyword_Id(requestId, keyword.getId()))
        .isEmpty();
  }

  @Test
  @DisplayName("다른 사용자의 요청에서 키워드를 삭제하면 404 이고 키워드 연결은 그대로 남는다")
  void deleteKeyword_fromOthersRequest_isNotFound() throws Exception {
    linkKeyword();

    mockMvc.perform(delete("/requests/{requestId}/keywords/{keywordId}", requestId,
            keyword.getId()).with(as(other)))
        .andExpect(status().isNotFound());

    assertThat(requestKeywordRepository.findByRequest_IdAndKeyword_Id(requestId, keyword.getId()))
        .isPresent();
  }

  // ---------- 요청 소유권: 본인 → 정상 ----------

  @Test
  @DisplayName("본인 요청에는 Spotify 곡을 추가할 수 있다(201)")
  void addSpotifyTrack_toOwnRequest_isCreated() throws Exception {
    // 이미 색인된 곡이라 OpenAI/Qdrant 호출 없이 연결만 만든다.
    mockMvc.perform(post("/requests/{requestId}/tracks", requestId).with(as(owner))
            .contentType(MediaType.APPLICATION_JSON)
            .content(trackJson(track.getSpotifyId())))
        .andExpect(status().isCreated());

    assertThat(requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, track.getId()))
        .isPresent();
  }

  @Test
  @DisplayName("본인 요청에는 곡을 추가할 수 있다(201)")
  void addTrack_toOwnRequest_isCreated() throws Exception {
    mockMvc.perform(post("/requests/{requestId}/tracks/{trackId}", requestId, track.getId())
            .with(as(owner)))
        .andExpect(status().isCreated());

    assertThat(requestTrackRepository.findByRequest_IdAndTrack_Id(requestId, track.getId()))
        .isPresent();
  }

  @Test
  @DisplayName("본인 요청에는 키워드를 추가하고 삭제할 수 있다(201, 204)")
  void addAndDeleteKeyword_onOwnRequest() throws Exception {
    mockMvc.perform(post("/requests/{requestId}/keywords/{keywordId}", requestId, keyword.getId())
            .with(as(owner)))
        .andExpect(status().isCreated());
    assertThat(requestKeywordRepository.findByRequest_IdAndKeyword_Id(requestId, keyword.getId()))
        .isPresent();

    mockMvc.perform(delete("/requests/{requestId}/keywords/{keywordId}", requestId,
            keyword.getId()).with(as(owner)))
        .andExpect(status().isNoContent());
    assertThat(requestKeywordRepository.findByRequest_IdAndKeyword_Id(requestId, keyword.getId()))
        .isEmpty();
  }

  // ---------- 플레이리스트 공개 범위 ----------

  @Test
  @DisplayName("다른 사용자의 비공개 플레이리스트와 곡 목록은 조회할 수 없다(404)")
  void privatePlaylist_ofOtherUser_isNotFound() throws Exception {
    Long playlistId = savePlaylist(PlaylistVisibility.PRIVATE);

    mockMvc.perform(get("/playlists/{playlistId}", playlistId).with(as(other)))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/playlists/{playlistId}/tracks", playlistId).with(as(other)))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("본인의 비공개 플레이리스트와 다른 사용자의 공개 플레이리스트는 조회할 수 있다(200)")
  void ownPrivateAndOthersPublicPlaylist_areReadable() throws Exception {
    Long privatePlaylistId = savePlaylist(PlaylistVisibility.PRIVATE);
    Long publicPlaylistId = savePlaylist(PlaylistVisibility.PUBLIC);

    mockMvc.perform(get("/playlists/{playlistId}", privatePlaylistId).with(as(owner)))
        .andExpect(status().isOk());
    mockMvc.perform(get("/playlists/{playlistId}/tracks", privatePlaylistId).with(as(owner)))
        .andExpect(status().isOk());
    mockMvc.perform(get("/playlists/{playlistId}", publicPlaylistId).with(as(other)))
        .andExpect(status().isOk());
    mockMvc.perform(get("/playlists/{playlistId}/tracks", publicPlaylistId).with(as(other)))
        .andExpect(status().isOk());
  }

  // ---------- helpers ----------

  // JwtAuthFilter 와 같은 모양의 인증 정보(JwtPrincipal + ROLE_ 권한)를 넣는다.
  private RequestPostProcessor as(User user) {
    JwtPrincipal principal = new JwtPrincipal(user.getId(), user.getRole().name());
    return authentication(new UsernamePasswordAuthenticationToken(principal, null,
        List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))));
  }

  private User saveUser(String prefix, UserRole role) {
    return userRepository.save(User.builder()
        .email(prefix + unique + "@test.local").username(prefix + unique.substring(0, 6))
        .password("pw").role(role).build());
  }

  private Long savePlaylist(PlaylistVisibility visibility) {
    return playlistRepository.save(Playlist.builder()
        .user(owner).title("playlist").visibility(visibility).deleted(false).build()).getId();
  }

  private void linkKeyword() {
    requestKeywordRepository.save(RequestKeyword.builder()
        .request(requestRepository.findById(requestId).orElseThrow()).keyword(keyword).build());
  }

  private String trackJson(String spotifyId) throws Exception {
    return objectMapper.writeValueAsString(TrackCreateRequestDto.builder()
        .spotifyId(spotifyId).name("Rain").build());
  }
}
