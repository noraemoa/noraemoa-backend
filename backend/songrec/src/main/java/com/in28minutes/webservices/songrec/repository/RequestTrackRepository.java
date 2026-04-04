package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.request.RequestTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.repository.projection.RequestTrackFeedbackRow;
import com.in28minutes.webservices.songrec.repository.projection.RecommendedTrackRow;
import com.in28minutes.webservices.songrec.repository.projection.RequestTrackCountRow;
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

  @Query("""
      select
          rt.request.id as requestId,
          count(rt.id) as trackCount
      from RequestTrack rt
      where rt.request.id in :requestIds
          and rt.trackDeleted=false
      group by rt.request.id
      """)
  List<RequestTrackCountRow> countActiveTracksByRequestIds(
      @Param("requestIds") List<Long> requestIds);

  @Query("""
      select
      t.id as trackId,
      t.spotifyId as spotifyId,
      t.name as trackName,
      t.artist as artistName,
      t.album as albumName,
      t.imageUrl as imageUrl,
      t.durationMs as durationMs,
      rtr.rating as rating
      from RequestTrack rt
      join rt.track t
      left join RequestTrackRating rtr
      on rtr.requestTrack = rt and rtr.user.id = :userId
      where rt.request.id = :requestId
      """)
  List<RecommendedTrackRow> findAllRecommendedTracksByRequestId(@Param("userId") Long userId,@Param("requestId")Long requestId);

  @Query("""
      select
      rt.track.id as trackId,
      rt.request.id as requestId,
      rt.avgRating as avgRating,
      rt.ratingCount as ratingCount
      from RequestTrack rt
      where rt.request.id in :requestIds
      and rt.track.id in :trackIds
      and rt.trackDeleted = false
      and rt.avgRating is not null
      and rt.ratingCount is not null
      and rt.ratingCount >0
      """)
  List<RequestTrackFeedbackRow> findFeedbackRowByRequestIdsAndTrackIds(@Param("requestIds") List<Long> requestIds,@Param("trackIds") List<Long> trackIds);
}
