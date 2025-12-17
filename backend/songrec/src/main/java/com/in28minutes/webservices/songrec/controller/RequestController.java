package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.*;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.*;
import com.in28minutes.webservices.songrec.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private final RequestService requestService;
    private final RequestTrackService requestTrackService;
    private final RequestKeywordService requestKeywordService;

    // requests
    @PostMapping
    public ResponseEntity<RequestResponseDto> createRequest(
            @Valid @RequestBody RequestCreateRequestDto requestDto,
            @PathVariable @NotNull @Positive Long userId) {
        Request request = requestService.createRequest(requestDto,userId);
        List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(userId, request.getId())
                .stream().map(KeywordResponseDto::from).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestResponseDto.from(request,keywords));
    }

    @PatchMapping("/{requestId}")
    public RequestResponseDto updateRequest(@Valid @RequestBody RequestCreateRequestDto requestDto,
                                            @PathVariable @NotNull @Positive Long userId,
                                            @PathVariable @NotNull @Positive Long requestId) {
        Request request = requestService.updateRequest(requestDto,userId,requestId);
        List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(userId, requestId)
                .stream().map(KeywordResponseDto::from).toList();
        return RequestResponseDto.from(request,keywords);
    }

    @GetMapping
    public List<RequestSummaryResponseDto> getRequests(@PathVariable @NotNull @Positive Long userId) {
        List<Request> requestList =  requestService.getRequestsByUserId(userId);

        return requestList.stream().map(RequestSummaryResponseDto::from).toList();
    }

    @GetMapping("/{requestId}")
    public RequestResponseDto getRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId){

        Request request = requestService.getActiveRequest(userId,requestId);
        List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(userId, request.getId())
                .stream().map(KeywordResponseDto::from).toList();

        return RequestResponseDto.from(request,keywords);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> deleteRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId){
        requestService.deleteRequest(userId,requestId);
        return ResponseEntity.noContent().build();
    }

    // tracks
    @GetMapping("/{requestId}/tracks")
    public List<TrackResponseDto> getTracksByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId) {
        List<Track> trackList = requestTrackService.getTracksByRequest(userId,requestId);
        return trackList
                .stream().map(TrackResponseDto::from)
                .toList();
    }

    @PostMapping("/{requestId}/tracks/{trackId}")
    public ResponseEntity<RequestTrackResponseDto> addTrackByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long trackId) {
        RequestTrack rt=  requestTrackService.addTrackByRequest(userId,requestId,trackId);
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestTrackResponseDto.from(rt));
    }

    @DeleteMapping("/{requestId}/tracks/{trackId}")
    public ResponseEntity<Void> deleteTrackByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long trackId) {

        requestTrackService.deleteTrack(userId,requestId,trackId);
        return ResponseEntity.noContent().build();
    }

    //keywords
    @PostMapping("/{requestId}/keywords/{keywordId}")
    public ResponseEntity<RequestKeywordResponseDto> addKeywordByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long keywordId) {
        RequestKeyword rk=  requestKeywordService.addKeywordByRequest(userId,requestId,keywordId);
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestKeywordResponseDto.from(rk));
    }

    @GetMapping("/{requestId}/keywords")
    public List<KeywordResponseDto> getKeywordsByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId) {
        List<Keyword> keywordsList = requestKeywordService.getKeywordsByRequest(userId,requestId);
        return keywordsList
                .stream().map(KeywordResponseDto::from)
                .toList();
    }
}
