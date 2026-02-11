package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.PlaylistTrack;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaylistTrackRepository extends JpaRepository<PlaylistTrack, Long> {
    @Query("""
select pt.track
from PlaylistTrack pt
where pt.playlist.id = :playlistId
  and pt.trackDeleted = false
""")
    List<Track> findActiveTracksByPlaylistId(@Param("playlistId") Long playlistId);
    Optional<PlaylistTrack> findByPlaylist_IdAndTrack_Id(Long playlistId, Long trackId);
}
