package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequestTrackRepository extends JpaRepository<RequestTrack, Long> {
    @Query("select rt.track from RequestTrack rt where rt.request.id = :requestId")
    List<Track> findTracksByRequestId(@Param("requestId") Long requestId);
}
