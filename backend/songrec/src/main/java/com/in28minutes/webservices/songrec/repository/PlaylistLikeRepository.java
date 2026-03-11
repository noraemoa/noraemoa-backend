package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.like.PlaylistLike;
import com.in28minutes.webservices.songrec.repository.projection.LikedPlaylistRow;
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
      order by pl.createdAt desc 
      """)
  List<LikedPlaylistRow> findLikedPlaylists(Long userId);
}
