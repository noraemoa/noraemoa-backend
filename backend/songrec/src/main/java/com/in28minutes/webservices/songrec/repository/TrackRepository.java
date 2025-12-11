package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
}
