package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.dto.response.request.RequestFeedItemDto;
import com.in28minutes.webservices.songrec.repository.projection.RequestKeywordRow;
import com.in28minutes.webservices.songrec.repository.projection.RequestTrackCountRow;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestFeedService {
    private final RequestService requestService;
    private final RequestKeywordService requestKeywordService;
    private final RequestTrackService requestTrackService;

    public List<RequestFeedItemDto> getFeed(int page, int size){
        Page<Request> requestPage = requestService.getAllRequests(page,size);
        List<Request> requests = requestPage.getContent();

        List<Long> requestIds = requests.stream().map(Request::getId).toList();
        if(requestIds.isEmpty()) return List.of();

        List<RequestKeywordRow> keywordRows = requestKeywordService.getAllKeywordsByRequests(requestIds);
        List<RequestTrackCountRow> countRows = requestTrackService.getTrackCountsByRequests(requestIds);

        Map<Long,List<RequestKeywordRow>> keywordsByRequestId=
                keywordRows.stream().collect(Collectors.groupingBy(RequestKeywordRow::getRequestId));
        Map<Long,Integer> trackCountByRequestId = countRows.stream().collect(
                Collectors.toMap(
                        RequestTrackCountRow::getRequestId,
                        r->r.getTrackCount().intValue()
                )
        );

        return requests.stream().map(r->{
            List<RequestKeywordRow> rowsForThis = keywordsByRequestId.getOrDefault(r.getId(),List.of());
            Integer trackCount = trackCountByRequestId.getOrDefault(r.getId(),0);
            return RequestFeedItemDto.from(r,rowsForThis,trackCount);
        }).toList();
    }
}
