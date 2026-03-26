package com.in28minutes.webservices.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistTemplate;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.response.playlist.PlaylistWithLikedResponseDto;
import com.in28minutes.webservices.songrec.fixture.UserFixture;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.PlaylistLikeRepository;
import com.in28minutes.webservices.songrec.repository.PlaylistRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceTest {

  @InjectMocks
  private PlaylistService playlistService;

  @Mock
  private PlaylistRepository playlistRepository;
  @Mock
  private PlaylistTemplateService playlistTemplateService;
  @Mock
  private EntityManager entityManager;
  @Mock
  private PlaylistLikeRepository playlistLikeRepository;

  @Test
  @DisplayName("기본 플레이리스트를 생성한다.")
  void createBasicPlaylists_success() {
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    PlaylistTemplate template1 = PlaylistTemplate.builder().id(1L).displayName("Template 1")
        .build();
    PlaylistTemplate template2 = PlaylistTemplate.builder().id(2L).displayName("Template 2")
        .build();

    when(entityManager.getReference(User.class, userId)).thenReturn(userRef);
    when(playlistTemplateService.getBasicPlaylistTemplates()).thenReturn(
        List.of(template1, template2));
    when(playlistRepository.existsByUser_IdAndTemplate_Id(userId, 1L)).thenReturn(false);
    when(playlistRepository.existsByUser_IdAndTemplate_Id(userId, 2L)).thenReturn(false);

    playlistService.createBasicPlaylists(userId);
    verify(playlistRepository, times(2)).save(any(Playlist.class));
  }

  @Test
  @DisplayName("사용자가 소유한 플레이리스트 중 좋아요한 플레이리스트는 liked=true, 아닌 건 false로 반환한다.")
  void getPlaylistsByUserId_setsLikedTrueForLikedPlaylists() {
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    Playlist playlist1 = Playlist.builder().id(1L).user(userRef).deleted(false).build();
    Playlist playlist2 = Playlist.builder().id(2L).user(userRef).deleted(false).build();
    Playlist playlist3 = Playlist.builder().id(3L).user(userRef).deleted(false).build();

    List<Playlist> playlists = List.of(playlist1, playlist2, playlist3);
    List<Long> likedIds = List.of(1L, 3L);

    when(playlistLikeRepository.findLikedPlaylistIds(userId)).thenReturn(likedIds);
    when(playlistRepository.findAllByUserIdAndDeletedFalse(userId)).thenReturn(playlists);

    List<PlaylistWithLikedResponseDto> response = playlistService.getPlaylistsByUserId(userId);

    assertThat(response).hasSize(3);
    assertThat(response.get(0).isLiked()).isTrue();
    assertThat(response.get(1).isLiked()).isFalse();
    assertThat(response.get(2).isLiked()).isTrue();

    verify(playlistLikeRepository).findLikedPlaylistIds(userId);
    verify(playlistRepository).findAllByUserIdAndDeletedFalse(userId);
  }

  @Test
  @DisplayName("좋아하는 플레이리스트가 없으면 모든 liked=false를 반환한다.")
  void getPlaylistsByUserId_whenNoLikedIds_thenAllFalse() {
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    Playlist playlist1 = Playlist.builder().id(1L).user(userRef).deleted(false).build();
    Playlist playlist2 = Playlist.builder().id(2L).user(userRef).deleted(false).build();
    Playlist playlist3 = Playlist.builder().id(3L).user(userRef).deleted(false).build();

    List<Playlist> playlists = List.of(playlist1, playlist2, playlist3);
    List<Long> likedIds = List.of();

    when(playlistLikeRepository.findLikedPlaylistIds(userId)).thenReturn(likedIds);
    when(playlistRepository.findAllByUserIdAndDeletedFalse(userId)).thenReturn(playlists);

    List<PlaylistWithLikedResponseDto> response = playlistService.getPlaylistsByUserId(userId);

    assertThat(response).hasSize(3);
    assertThat(response.get(0).isLiked()).isFalse();
    assertThat(response.get(1).isLiked()).isFalse();
    assertThat(response.get(2).isLiked()).isFalse();

    verify(playlistLikeRepository).findLikedPlaylistIds(userId);
    verify(playlistRepository).findAllByUserIdAndDeletedFalse(userId);
  }

  @Test
  @DisplayName("사용자가 소유한 플레이리스트가 없으면 빈 배열을 반환한다.")
  void getPlaylistsByUserId_whenNoPlaylists_thenReturnEmptyList() {
    Long userId = 1L;

    List<Playlist> playlists = List.of();
    List<Long> likedIds = List.of();

    when(playlistLikeRepository.findLikedPlaylistIds(userId)).thenReturn(likedIds);
    when(playlistRepository.findAllByUserIdAndDeletedFalse(userId)).thenReturn(playlists);

    List<PlaylistWithLikedResponseDto> response = playlistService.getPlaylistsByUserId(userId);

    assertThat(response).hasSize(0);

    verify(playlistLikeRepository).findLikedPlaylistIds(userId);
    verify(playlistRepository).findAllByUserIdAndDeletedFalse(userId);
  }

  @Test
  @DisplayName("특정 플레이리스트의 세부 사항과 좋아요 여부를 반환한다.")
  void getAccessiblePlaylistDetails_whenLikedId_thenReturnTrue() {
    Long playlistId = 1L;
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    Playlist playlist = Playlist.builder().id(playlistId).user(userRef).deleted(false).build();
    when(playlistRepository.findByIdAndDeletedFalse(playlistId)).thenReturn(Optional.of(playlist));
    when(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId)).thenReturn(true);
    PlaylistWithLikedResponseDto response = playlistService.getAccessiblePlaylistDetails(userId,
        playlistId);
    assertThat(response.isLiked()).isTrue();
  }

  @Test
  @DisplayName("좋아요하지 않은 플레이리스트는 liked=false를 반환한다.")
  void getAccessiblePlaylistDetails_whenNotLiked_thenReturnFalse() {
    Long playlistId = 1L;
    Long userId = 1L;
User userRef = UserFixture.userRef(userId);
    Playlist playlist = Playlist.builder().id(playlistId).user(userRef).deleted(false).build();

    when(playlistRepository.findByIdAndDeletedFalse(playlistId))
        .thenReturn(Optional.of(playlist));
    when(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId))
        .thenReturn(false);

    PlaylistWithLikedResponseDto response =
        playlistService.getAccessiblePlaylistDetails(userId, playlistId);

    assertThat(response.isLiked()).isFalse();
  }

  @Test
  @DisplayName("특정 플레이리스트를 찾을 수 없으면 예외를 던진다.")
  void getAccessiblePlaylistDetails_whenNotFound_thenThrowException() {
    Long playlistId = 1L;
    Long userId = 1L;

    when(playlistRepository.findByIdAndDeletedFalse(playlistId)).thenReturn(Optional.empty());

    assertThatThrownBy(()-> playlistService.getAccessiblePlaylistDetails(userId, playlistId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("해당 플레이리스트를 찾을 수 없습니다.");
  }

  @Test
  @DisplayName("사용자가 소유한 플레이리스트는 삭제할 수 있다.")
  void deletePlaylist_whenOwned_thenReturnDeletedTrue() {
    Long playlistId = 1L;
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    Playlist playlist = Playlist.builder().id(playlistId).user(userRef).deleted(false).build();
    when(playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId, userId)).thenReturn(
        Optional.of(playlist));
    playlistService.deletePlaylist(playlistId, userId);
    assertThat(playlist.getDeleted()).isTrue();
  }

  @Test
  @DisplayName("사용자가 소유하지 않은 플레이리스트를 삭제할 경우 예외를 던진다.")
  void deletePlaylist_whenNotOwned_thenThrowException() {
    Long playlistId = 1L;
    Long userId = 1L;

    when(playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId, userId)).thenReturn(
        Optional.empty());

    assertThatThrownBy(()-> playlistService.deletePlaylist(playlistId, userId))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("해당 플레이리스트를 찾을 수 없습니다.");
  }

  @Test
  void updateVisibility_whenOwned_thenUpdateVisibility() {
    Long playlistId = 1L;
    Long userId = 1L;
    PlaylistVisibility visibility = PlaylistVisibility.PUBLIC;
    Playlist playlist = Playlist.builder().id(playlistId).deleted(false).build();
    when(playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId, userId)).thenReturn(
        Optional.of(playlist));
    Playlist response = playlistService.updateVisibility(userId, playlistId, visibility);
    assertThat(response.getVisibility()).isEqualTo(visibility);
  }

  @Test
  void updateVisibility_whenNotOwned_thenThrowException() {
    Long playlistId = 1L;
    Long userId = 1L;
    PlaylistVisibility visibility = PlaylistVisibility.PRIVATE;
    when(playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId, userId)).thenReturn(
        Optional.empty());

    assertThatThrownBy(()-> playlistService.updateVisibility(userId, playlistId, visibility))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("해당 플레이리스트를 찾을 수 없습니다.");
  }

  @Test
  void getAuthPublicPlaylists_setsLikedTrueForLikedPublicPlaylists() {
    Long userId = 1L;
    User userRef = UserFixture.userRef(userId);
    PlaylistVisibility visibility = PlaylistVisibility.PUBLIC;
    Playlist playlist1 = Playlist.builder().id(1L).user(userRef).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();
    Playlist playlist2 = Playlist.builder().id(2L).user(userRef).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();
    Playlist playlist3 = Playlist.builder().id(3L).user(userRef).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();

    Page<Playlist> playlists = new PageImpl<>(List.of(playlist1, playlist2, playlist3));
    List<Long> likedIds = List.of(1L,3L);

    Pageable pageable = PageRequest.of(0, 20, Sort.by(Direction.DESC, "id"));
    when(playlistRepository.findAllByVisibilityAndDeletedFalse(visibility, pageable)).thenReturn(playlists);
    when(playlistLikeRepository.findLikedPlaylistIds(userId)).thenReturn(likedIds);

    List<PlaylistWithLikedResponseDto> response = playlistService.getAuthPublicPlaylists(pageable,userId);
    assertThat(response).hasSize(3);
    assertThat(response.get(0).isLiked()).isTrue();
    assertThat(response.get(1).isLiked()).isFalse();
    assertThat(response.get(2).isLiked()).isTrue();
  }

  @Test
  void getAuthPublicPlaylists_whenNoLikedIds_thenAllFalse() {
    Long userId = 1L;
    User user = UserFixture.userRef(userId);
    PlaylistVisibility visibility = PlaylistVisibility.PUBLIC;
    Playlist playlist1 = Playlist.builder().id(1L).user(user).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();
    Playlist playlist2 = Playlist.builder().id(2L).user(user).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();
    Playlist playlist3 = Playlist.builder().id(3L).user(user).visibility(PlaylistVisibility.PUBLIC)
        .deleted(false).build();

    Page<Playlist> playlists = new PageImpl<>(List.of(playlist1, playlist2, playlist3));
    List<Long> likedIds = List.of();

    Pageable pageable = PageRequest.of(0, 20, Sort.by(Direction.DESC, "id"));
    when(playlistRepository.findAllByVisibilityAndDeletedFalse(visibility, pageable)).thenReturn(playlists);
    when(playlistLikeRepository.findLikedPlaylistIds(userId)).thenReturn(likedIds);

    List<PlaylistWithLikedResponseDto> response = playlistService.getAuthPublicPlaylists(pageable,userId);
    assertThat(response).hasSize(3);
    assertThat(response.get(0).isLiked()).isFalse();
    assertThat(response.get(1).isLiked()).isFalse();
    assertThat(response.get(2).isLiked()).isFalse();
  }

}
