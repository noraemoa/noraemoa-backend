package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.request.RequestTrackRating;
import com.in28minutes.webservices.songrec.repository.projection.RecommendedTrackRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequestTrackRatingRepository extends JpaRepository<RequestTrackRating, Long> {

  Optional<RequestTrackRating> findByRequestTrack_IdAndUser_Id(Long requestTrackId, Long userId);
}
