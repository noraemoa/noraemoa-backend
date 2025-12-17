package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.*;
import com.in28minutes.webservices.songrec.dto.response.KeywordResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestKeywordRepository;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestKeywordService {
    private final RequestKeywordRepository requestKeywordRepository;
    private final RequestService requestService;
    private final KeywordService keywordService;

    @Transactional(readOnly = true)
    public List<Keyword> getKeywordsByRequest(Long userId, Long requestId) {
        requestService.getActiveRequest(userId, requestId); //userId 검증용

        return requestKeywordRepository.findAllKeywordsByRequestId(requestId);
    }

    @Transactional
    public RequestKeyword addKeywordByRequest(Long userId, Long requestId, Long keywordId) {
        Request request = requestService.getActiveRequest(userId, requestId);
        Keyword keyword = keywordService.getKeyword(keywordId);

        return requestKeywordRepository.findByRequest_IdAndKeyword_Id(requestId,keywordId)
                .orElseGet(()->requestKeywordRepository.save(
                        RequestKeyword.builder()
                                .request(request)
                                .keyword(keyword).build()
                ));
    }
}
