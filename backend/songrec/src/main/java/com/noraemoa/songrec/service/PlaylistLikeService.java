package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.like.PlaylistLike;
import com.noraemoa.songrec.domain.playlist.Playlist;
import com.noraemoa.songrec.domain.playlist.PlaylistVisibility;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.response.LikedPlaylistItemDto;
import com.noraemoa.songrec.global.exception.ConflictException;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.PlaylistLikeRepository;
import com.noraemoa.songrec.repository.PlaylistRepository;
import com.noraemoa.songrec.repository.projection.LikedPlaylistRow;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaylistLikeService {

  private final PlaylistLikeRepository playlistLikeRepository;
  private final PlaylistRepository playlistRepository;
  private final EntityManager entityManager;

  @Transactional
  public PlaylistLike addPlaylistLike(Long userId,Long playlistId){
    if(playlistLikeRepository.existsByUser_IdAndPlaylist_Id(userId,playlistId)){
      throw new ConflictException("이미 좋아한 플레이리스트입니다.");
    }

    Playlist playlist=playlistRepository.findByIdAndVisibilityAndDeletedFalse(playlistId, PlaylistVisibility.PUBLIC)
        .orElseThrow(()->new NotFoundException("플레이리스트를 찾을 수 없습니다."));
    User userRef=entityManager.getReference(User.class, userId);

    PlaylistLike playlistLike=PlaylistLike.builder()
        .user(userRef)
        .playlist(playlist)
        .build();
    return playlistLikeRepository.save(playlistLike);
  }

  @Transactional
  public void removePlaylistLike(Long userId,Long playlistId){
    playlistLikeRepository.deleteByUser_IdAndPlaylist_Id(userId,playlistId);
  }

  @Transactional(readOnly = true)
  public List<LikedPlaylistItemDto> getPlaylistLikes(Long userId){
    List<LikedPlaylistRow> playlistRows = playlistLikeRepository.findLikedPlaylists(userId);
    return playlistRows.stream().map(LikedPlaylistItemDto::from).toList();
  }
}
