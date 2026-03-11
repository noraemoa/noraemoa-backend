package com.in28minutes.webservices.songrec.repository;

import com.in28minutes.webservices.songrec.domain.like.TrackLike;
import com.in28minutes.webservices.songrec.repository.projection.LikedTrackRow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TrackLikeRepository extends JpaRepository<TrackLike, Long> {
    boolean existsByUser_IdAndTrack_Id(Long userId, Long trackId);
    void deleteByUser_IdAndTrack_Id(Long userId, Long trackId);
    @Query("""
        select 
        t.id as trackId,
        t.name as name,
        t.artist as artist,
        t.album as album,
        t.imageUrl as imageUrl,
        tl.createdAt as createdAt
        from TrackLike tl
        join tl.track t
        where tl.user.id = :userId
        order by tl.createdAt desc
        """)
    List<LikedTrackRow> findLikedTracks(Long userId);

    @Query("""
select t.spotifyId
from TrackLike tl
join tl.track t
where tl.user.id = :userId
and t.spotifyId in :spotifyTrackIds
""")
    List<String>findLikedSpotifyIds(Long userId,List<String> spotifyTrackIds);

}
