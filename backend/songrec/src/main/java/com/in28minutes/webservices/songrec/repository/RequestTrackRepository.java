package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RequestTrackRepository extends JpaRepository<RequestTrack, Long> {
    @Query("""
select rt.track
from RequestTrack rt
where rt.request.id = :requestId
  and rt.trackDeleted = false
""")
    List<Track> findActiveTracksByRequestId(@Param("requestId") Long requestId);

    Optional<RequestTrack> findByRequest_IdAndTrack_Id(Long requestId, Long trackId);
}
