package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.track.Track;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface TrackRepository extends JpaRepository<Track, Long> {
    Optional<Track> findBySpotifyId(String spotifyId);

    @Query("""
select t.id
from Track t
where t.spotifyId in :spotifyIds
""")
    List<Long> findSearchTrackIds(List<String> spotifyIds);
    List<Track> findAllByIdIn(Collection<Long>ids);
}
