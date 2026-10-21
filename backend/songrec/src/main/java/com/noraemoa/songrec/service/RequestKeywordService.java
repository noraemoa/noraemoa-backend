package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.keyword.Keyword;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.request.RequestKeyword;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.RequestKeywordRepository;
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
    private final RequestOwnershipService requestOwnershipService;

    @Transactional(readOnly = true)
    public List<Keyword> getKeywordsByRequest(Long requestId) {

        return requestKeywordRepository.findAllKeywordsByRequestId(requestId);
    }

    @Transactional(readOnly = true)
    public List<RequestKeywordRow> getAllKeywordsByRequests(List<Long> requestIds) {

        return requestKeywordRepository.findKeywordRowsByRequestIds(requestIds);
    }

    // 본인 요청에만 키워드를 추가할 수 있다(소유자가 아니면 404).
    @Transactional
    public RequestKeyword addKeywordByRequest(Long userId, Long requestId, Long keywordId) {
        Request request = requestOwnershipService.getOwnedRequest(userId, requestId);
        Keyword keyword = keywordService.getKeyword(keywordId);

        return requestKeywordRepository.findByRequest_IdAndKeyword_Id(request.getId(),keyword.getId())
                .orElseGet(()->requestKeywordRepository.save(
                        RequestKeyword.builder()
                                .request(request)
                                .keyword(keyword).build()
                ));
    }

    // 본인 요청의 키워드만 삭제할 수 있다(소유자가 아니면 404).
    @Transactional
    public void deleteKeywordByRequestId(Long userId, Long requestId, Long keywordId) {
        requestOwnershipService.getOwnedRequest(userId, requestId);
        requestKeywordRepository.deleteByRequest_idAndKeyword_Id(requestId,keywordId);
    }
}
