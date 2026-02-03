package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KeywordTrackRepository extends JpaRepository<KeywordTrack, Long> {
    @Query("""
select kt.track
from KeywordTrack kt
where kt.keyword.id = :keywordId
""")
    List<Track> findAllTracksByKeywordId(@Param("keywordId") Long keywordId);
    Optional<KeywordTrack> findByKeyword_IdAndTrack_Id(Long keywordId,Long trackId);

    @Query("""
select kt.track as track, max(kt.recommendCount) as score
from KeywordTrack kt
where kt.keyword.id in (
select rk.keyword.id
from RequestKeyword rk
where rk.request.id=:requestId
)
and kt.track.id not in (
select rt.track.id
from RequestTrack rt
where rt.request.id=:requestId
and rt.trackDeleted=false
)
group by kt.track
order by max(kt.recommendCount) desc, min(kt.id) asc
""")
    Slice<Track> findTracksByKeywordExcludeRequest(
            @Param("requestId") Long requestId,
            Pageable pageable
    );
}
