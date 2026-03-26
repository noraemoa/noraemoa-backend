package com.in28minutes.webservices.songrec.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.in28minutes.webservices.songrec.domain.like.PlaylistLike;
import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.fixture.UserFixture;
import com.in28minutes.webservices.songrec.global.exception.ConflictException;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.PlaylistLikeRepository;
import com.in28minutes.webservices.songrec.repository.PlaylistRepository;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PlaylistLikeServiceTest {

  @InjectMocks
  private PlaylistLikeService playlistLikeService;

  @Mock
  private PlaylistLikeRepository playlistLikeRepository;
  @Mock
  private PlaylistRepository playlistRepository;
  @Mock
  private EntityManager entityManager;

  @Test
  @DisplayName("공개 플레이리스트에 좋아요를 추가할 수 있다.")
  void addPlaylistLike_success() {
    Long userId = 1L;
    Long playlistId = 2L;

    Playlist playlist = Playlist.builder().id(2L).build();
    User user = UserFixture.userRef(userId);

    when(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId))
        .thenReturn(false);
    when(playlistRepository.findByIdAndVisibilityAndDeletedFalse(playlistId,
        PlaylistVisibility.PUBLIC))
        .thenReturn(Optional.of(playlist));
    when(entityManager.getReference(User.class, userId)).thenReturn(user);

    ArgumentCaptor<PlaylistLike> captor = ArgumentCaptor.forClass(PlaylistLike.class);
    playlistLikeService.addPlaylistLike(1L, 2L);
    verify(playlistLikeRepository).save(captor.capture());

    PlaylistLike playlistLike = captor.getValue();
    assertThat(playlistLike.getUser().getId()).isEqualTo(userId);
    assertThat(playlistLike.getPlaylist().getId()).isEqualTo(playlistId);

  }

  @Test
  @DisplayName("이미 좋아요한 플레이리스트는 예외를 던진다.")
  void addPlaylistLike_whenLiked_thenThrowConflictException() {
    Long userId = 1L;
    Long playlistId = 2L;
    when(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId))
        .thenReturn(true);

    assertThatThrownBy(() -> playlistLikeService.addPlaylistLike(1L, 2L))
        .isInstanceOf(ConflictException.class)
        .hasMessage("이미 좋아한 플레이리스트입니다.");
  }

  @Test
  @DisplayName("공개 플레이리스트가 아니거나 존재하지 않는 플레이리스트는 예외를 던진다.")
  void addPlaylistLike_whenPlaylistNotFound_thenThrowNotFoundException() {
    Long userId = 1L;
    Long playlistId = 2L;
    when(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId))
        .thenReturn(false);
    when(playlistRepository.findByIdAndVisibilityAndDeletedFalse(playlistId,
        PlaylistVisibility.PUBLIC))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> playlistLikeService.addPlaylistLike(1L, 2L))
        .isInstanceOf(NotFoundException.class)
        .hasMessage("플레이리스트를 찾을 수 없습니다.");
  }

}
