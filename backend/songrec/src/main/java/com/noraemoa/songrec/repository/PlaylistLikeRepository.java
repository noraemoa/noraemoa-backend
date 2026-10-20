package com.noraemoa.songrec.repository;

import com.noraemoa.songrec.domain.like.PlaylistLike;
import com.noraemoa.songrec.repository.projection.LikedPlaylistRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PlaylistLikeRepository extends JpaRepository<PlaylistLike, Long> {

  boolean existsByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

  void deleteByUser_IdAndPlaylist_Id(Long userId, Long playlistId);

  @Query("""
      select 
      p.id as playlistId,
      p.user.username as username,
      p.title as playlistTitle,
      p.thumbnailUrl as thumbnailUrl,
      pl.createdAt as createdAt
      from PlaylistLike pl
      join pl.playlist p
      where pl.user.id = :userId
      and p.deleted = false
      and p.visibility = com.noraemoa.songrec.domain.playlist.PlaylistVisibility.PUBLIC
      order by pl.createdAt desc 
      """)
  List<LikedPlaylistRow> findLikedPlaylists(Long userId);

  @Query("""
      select p.id
      from PlaylistLike pl
      join pl.playlist p
      where pl.user.id = :userId
      and p.deleted = false
      and p.visibility = com.noraemoa.songrec.domain.playlist.PlaylistVisibility.PUBLIC
      order by pl.createdAt desc 
      """)
  List<Long> findLikedPlaylistIds(Long userId);

}
