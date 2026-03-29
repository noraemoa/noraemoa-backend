package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistTemplate;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import com.in28minutes.webservices.songrec.dto.request.PlaylistCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.playlist.PlaylistWithLikedResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.PlaylistLikeRepository;
import com.in28minutes.webservices.songrec.repository.PlaylistRepository;
import com.in28minutes.webservices.songrec.repository.projection.PopularPlaylistRow;
import com.in28minutes.webservices.songrec.service.fileStorage.FileStorageService;
import com.in28minutes.webservices.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final PlaylistTemplateService playlistTemplateService;
  private final EntityManager entityManager;
  private final FileStorageService fileStorageService;
  private final PlaylistLikeRepository playlistLikeRepository;

  @Transactional
  public void createBasicPlaylists(Long userId) {
    User userRef = entityManager.getReference(User.class, userId);
    List<PlaylistTemplate> playlistTemplates = playlistTemplateService.getBasicPlaylistTemplates();
    for (PlaylistTemplate t : playlistTemplates) {
      if (!playlistRepository.existsByUser_IdAndTemplate_Id(userId, t.getId())) {
        playlistRepository.save(Playlist.builder()
            .user(userRef)
            .template(t)
            .title(t.getDisplayName())
            .deleted(false)
            .build()
        );
      }
    }
  }

  @Transactional(readOnly = true)
  public List<PlaylistWithLikedResponseDto> getPlaylistsByUserId(Long userId) {
    Set<Long> likedPlaylistIds = new HashSet<>(playlistLikeRepository.findLikedPlaylistIds(userId));
    List<Playlist> playlists= playlistRepository.findAllByUserIdAndDeletedFalse(userId);
    return playlists.stream().map(p->PlaylistWithLikedResponseDto.from(p,likedPlaylistIds.contains(p.getId()))).toList();
  }

  @Transactional
  public Playlist createPlaylist(Long userId, PlaylistCreateRequestDto playlistDto) {
    User userRef = entityManager.getReference(User.class, userId);
    Playlist playlist = Playlist.builder()
        .user(userRef)
        .title(playlistDto.getTitle())
        .visibility(PlaylistVisibility.PUBLIC)
        .deleted(false)
        .build();

    return playlistRepository.save(playlist);
  }

  @Transactional(readOnly = true)
  public Playlist getAccessiblePlaylist(Long playlistId) {
    return playlistRepository.findByIdAndDeletedFalse(playlistId)
        .orElseThrow(() -> new NotFoundException("해당 플레이리스트를 찾을 수 없습니다."));
  }

  @Transactional(readOnly = true)
  public PlaylistWithLikedResponseDto getAccessiblePlaylistDetails(Long userId,Long playlistId) {
    Playlist playlist = getAccessiblePlaylist(playlistId);
    boolean liked = playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId, playlistId);
    return PlaylistWithLikedResponseDto.from(playlist,liked);
  }

  @Transactional(readOnly = true)
  public Playlist getOwnedPlaylist(Long userId, Long playlistId) {
    return playlistRepository.findByIdAndUserIdAndDeletedFalse(playlistId, userId)
        .orElseThrow(() -> new NotFoundException("해당 플레이리스트를 찾을 수 없습니다."));
  }

  @Transactional
  public Playlist updatePlaylist(PlaylistCreateRequestDto playlistDto, Long userId,
      Long playlistId) {
    Playlist playlist = getOwnedPlaylist(userId, playlistId);
    playlist.setTitle(playlistDto.getTitle());
    return playlist;
  }

  @Transactional
  public void deletePlaylist(Long userId, Long playlistId) {
    Playlist playlist = getOwnedPlaylist(userId, playlistId);
    playlist.setDeleted(true);
  }

  @Transactional
  public Playlist uploadThumbnail(Long userId, Long playlistId, MultipartFile file)
      throws IOException {
    Playlist playlist = getOwnedPlaylist(userId, playlistId);

    StoredFile stored =
        fileStorageService.storePlaylistThumbnail(playlistId, file);

    playlist.setThumbnailKey(stored.key());
    playlist.setThumbnailUrl(stored.url());
    return playlist;
  }

  @Transactional
  public Playlist updateVisibility(Long userId, Long playlistId,
      PlaylistVisibility playlistVisibility) {
    Playlist playlist = getOwnedPlaylist(userId, playlistId);
    playlist.setVisibility(playlistVisibility);
    return playlist;
  }

  @Transactional(readOnly = true)
  public Page<Playlist> getPublicPlaylists(Pageable pageable) {
    return playlistRepository.findAllByVisibilityAndDeletedFalse(PlaylistVisibility.PUBLIC,
        pageable);

  }

  @Transactional(readOnly = true)
  public List<PlaylistWithLikedResponseDto> getAuthPublicPlaylists(Pageable pageable, Long userId) {
    Page<Playlist> playlists = playlistRepository.findAllByVisibilityAndDeletedFalse(
        PlaylistVisibility.PUBLIC, pageable);
    List<Long> likedPlaylistIds = playlistLikeRepository.findLikedPlaylistIds(userId);

    return playlists.map(p-> PlaylistWithLikedResponseDto.from(p, likedPlaylistIds.contains(p.getId()))).stream().toList();
  }

  @Transactional(readOnly = true)
  public Page<PopularPlaylistRow> getPopularPlaylists(Pageable pageable) {
    return playlistRepository.findPopularPlaylists(pageable);
  }
}
