package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.Track;
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
}
