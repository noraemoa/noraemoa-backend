package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.repository.KeywordTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<Track> getTracksByKeyword(Long keywordId) {
        return keywordTrackRepository.findAllTracksByKeywordId(keywordId);
    }
}
