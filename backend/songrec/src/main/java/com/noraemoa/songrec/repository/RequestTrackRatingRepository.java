package com.noraemoa.songrec.repository;

import com.noraemoa.songrec.domain.request.RequestTrackRating;
import com.noraemoa.songrec.repository.projection.RecommendedTrackRow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RequestTrackRatingRepository extends JpaRepository<RequestTrackRating, Long> {

  Optional<RequestTrackRating> findByRequestTrack_IdAndUser_Id(Long requestTrackId, Long userId);
}
