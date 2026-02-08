package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.KeywordTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KeywordTrackService {
    private final KeywordTrackRepository keywordTrackRepository;
    private final KeywordService keywordService;
    private final TrackService trackService;

    @Transactional
    public KeywordTrack addTrackByKeyword(Long keywordId, Long trackId) {
        Keyword keyword = keywordService.getKeyword(keywordId);
        Track track = trackService.getTrack(trackId);
        return keywordTrackRepository.findByKeyword_IdAndTrack_Id(keywordId,trackId)
                .orElseGet(()-> keywordTrackRepository.save(
                        KeywordTrack.builder()
                                .keyword(keyword)
                                .track(track)
                                .build())
        );
    }

    @Transactional
    public KeywordTrack recommendTrack(Long keywordId, Long trackId){
        Optional<KeywordTrack> keywordTrack = keywordTrackRepository.findByKeyword_IdAndTrack_Id(keywordId, trackId);
        if(keywordTrack.isPresent()){
            keywordTrack.get().setRecommendCount(keywordTrack.get().getRecommendCount()+1);
            return keywordTrackRepository.save(keywordTrack.get());
        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<Track> getTracksByKeyword(Long keywordId) {
        return keywordTrackRepository.findAllTracksByKeywordId(keywordId);
    }

    @Transactional(readOnly = true)
    public KeywordTrack getKeywordTrack(Long keywordId,Long trackId) {
        return keywordTrackRepository.findByKeyword_IdAndTrack_Id(keywordId,trackId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Slice<Track> getRecommendedTracks(
            Long requestId,
            int page,
            int size
    ){
        Pageable pageable = PageRequest.of(page,size);
        return keywordTrackRepository.findTracksByKeywordExcludeRequest(requestId,pageable);
    }

    @Transactional
    public void applyRatingDelta(Long keywordId,Long trackId,Integer oldRating, Integer newRating){
        KeywordTrack keywordTrack = keywordTrackRepository.findByKeyword_IdAndTrack_Id(keywordId,trackId)
                        .orElse(null);
        if(keywordTrack == null){return;}
        if(oldRating == null){
            keywordTrack.setRatingCount(keywordTrack.getRatingCount() + 1);
            keywordTrack.setRatingSum(keywordTrack.getRatingSum() + newRating);
        }else{
            keywordTrack.setRatingSum(keywordTrack.getRatingSum() + (newRating-oldRating));
        }

        if(keywordTrack.getRatingCount()>0)
            keywordTrack.setRatingAverage((double) keywordTrack.getRatingSum() /keywordTrack.getRatingCount());
        else
            keywordTrack.setRatingAverage(0.0);
    }
}
