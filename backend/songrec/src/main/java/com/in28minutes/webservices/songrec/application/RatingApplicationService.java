package com.in28minutes.webservices.songrec.application;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.service.KeywordTrackService;
import com.in28minutes.webservices.songrec.service.RequestKeywordService;
import com.in28minutes.webservices.songrec.service.RequestTrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingApplicationService {
    private final RequestTrackService  requestTrackService;
    private final RequestKeywordService requestKeywordService;
    private final KeywordTrackService keywordTrackService;

    @Transactional
    public RequestTrack rateTrack(
            Long userId,
            Long requestId,
            Long trackId,
            int rating
    ){
        RequestTrack requestTrack = requestTrackService.getActiveRequestTrack(userId, requestId, trackId);
        Integer oldRating = requestTrack.getRating();
        if(oldRating != null && oldRating == rating){
            return requestTrack;
        }

        requestTrackService.rateTrack(userId,requestId,trackId,rating);
        List<Keyword> keywords = requestKeywordService.getKeywordsByRequest(userId, requestId);
        keywords.forEach(keyword -> {
            keywordTrackService.applyRatingDelta(keyword.getId(),trackId, oldRating,rating);
        });

        return requestTrack;
    }
}
