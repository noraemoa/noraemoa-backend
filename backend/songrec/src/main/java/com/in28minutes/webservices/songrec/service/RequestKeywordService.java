package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.request.RequestKeyword;
import com.in28minutes.webservices.songrec.repository.RequestKeywordRepository;
import com.in28minutes.webservices.songrec.repository.projection.RequestKeywordRow;
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
    public List<Keyword> getKeywordsByRequest(Long requestId) {

        return requestKeywordRepository.findAllKeywordsByRequestId(requestId);
    }

    @Transactional(readOnly = true)
    public List<RequestKeywordRow> getAllKeywordsByRequests(List<Long> requestIds) {

        return requestKeywordRepository.findKeywordRowsByRequestIds(requestIds);
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

    @Transactional
    public void deleteKeywordByRequestId(Long requestId,Long keywordId) {
        requestKeywordRepository.deleteByRequest_idAndKeyword_Id(requestId,keywordId);
    }
}
