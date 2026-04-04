package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.playlist.Playlist;
import com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility;
import com.in28minutes.webservices.songrec.repository.projection.PopularPlaylistRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

  List<Playlist> findAllByUserIdAndDeletedFalse(Long userId);

  boolean existsByUser_IdAndTemplate_Id(Long userId, Long templateId);

  Optional<Playlist> findByIdAndVisibilityAndDeletedFalse(Long id, PlaylistVisibility visibility);

  Optional<Playlist> findByIdAndUserIdAndDeletedFalse(Long id, Long userId);

  Optional<Playlist> findByIdAndDeletedFalse(Long id);

  Page<Playlist> findAllByVisibilityAndDeletedFalse(PlaylistVisibility visibility,
      Pageable pageable);

  @Query("""
              select p from Playlist p
              where p.id = :playlistId
              and p.deleted = false
              and (p.user.id = :userId or p.visibility = com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility.PUBLIC)
      """)
  Optional<Playlist> findAccessiblePlaylist(
      @Param("playlistId") Long playlistId,
      @Param("userId") Long userId
  );

  @Query("""
      select 
      p.id as playlistId,
      p.user.id as userId,
      p.user.username as username,
      p.title as title,
      p.thumbnailUrl as thumbnailUrl,
      p.visibility as visibility,
      count(pl) as likeCount
      from Playlist p
      left join PlaylistLike pl on pl.playlist=p
      where p.visibility = com.in28minutes.webservices.songrec.domain.playlist.PlaylistVisibility.PUBLIC
      and p.deleted = false
      group by p
      order by count(pl) desc, p.id desc
      """)
  Page<PopularPlaylistRow> findPopularPlaylists(Pageable pageable);
}
