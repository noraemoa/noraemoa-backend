package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.keyword.Keyword;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.request.RequestKeyword;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.RequestKeywordRepository;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.projection.RequestKeywordRow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestKeywordService {
    private final RequestKeywordRepository requestKeywordRepository;
    private final KeywordService keywordService;
    private final RequestRepository requestRepository;

    @Transactional(readOnly = true)
    public List<Keyword> getKeywordsByRequest(Long requestId) {

        return requestKeywordRepository.findAllKeywordsByRequestId(requestId);
    }

    @Transactional(readOnly = true)
    public List<RequestKeywordRow> getAllKeywordsByRequests(List<Long> requestIds) {

        return requestKeywordRepository.findKeywordRowsByRequestIds(requestIds);
    }

    @Transactional
    public RequestKeyword addKeywordByRequest( Request request, Keyword keyword) {

        return requestKeywordRepository.findByRequest_IdAndKeyword_Id(request.getId(),keyword.getId())
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
