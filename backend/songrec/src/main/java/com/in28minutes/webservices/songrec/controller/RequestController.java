package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.application.RatingApplicationService;
import com.in28minutes.webservices.songrec.domain.*;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.RequestTrackRatingRequestDto;
import com.in28minutes.webservices.songrec.dto.response.*;
import com.in28minutes.webservices.songrec.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/requests")
public class RequestController {
    private static final Logger log = LoggerFactory.getLogger(RequestController.class);
    private final RequestService requestService;
    private final RequestTrackService requestTrackService;
    private final RequestKeywordService requestKeywordService;
    private final KeywordTrackService keywordTrackService;
    private final RatingApplicationService ratingApplicationService;

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

    // 키워드에 해당하는 트랙들을 받아서 추천도 순으로 정렬한 후 3개씩 받아와서 요청에 해당 트랙들 저장
    @PostMapping("/{requestId}/tracks/recommendations")
    public ResponseEntity<RecommendedTracksResponseDto> addRecommendedTracksToRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "3") @Positive int size
    ){
        Slice<Track> slice = keywordTrackService.getRecommendedTracks(requestId,page,size);
        log.info("recommended trackIds={}", slice.getContent().stream().map(Track::getId).toList());
        List<Track> tracks = slice.getContent();
        tracks.forEach(track -> {
            requestTrackService.addTrackByRequest(userId,requestId,track.getId());
        });
        return ResponseEntity.status(HttpStatus.CREATED).body(
                RecommendedTracksResponseDto.builder()
                        .tracks(tracks.stream().map(TrackResponseDto::from).toList())
                        .hasNext(slice.hasNext())
                        .nextPage(slice.hasNext()?page+1:page)
                        .build()
        );
    }

    //요청에 트랙 직접 추가
    @PostMapping("/{requestId}/tracks/{trackId}")
    public ResponseEntity<RequestTrackResponseDto> addTrackByRequest(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long trackId) {
        RequestTrack rt=  requestTrackService.addTrackByRequest(userId,requestId,trackId);

        // 해당 request의 keyword들과 이 track을 각각 연결 시키기 (해당 track을 고용 바구니에도 추가하는 작업)
        List<Keyword> keywords = requestKeywordService.getKeywordsByRequest(userId, requestId);
        keywords.forEach(keyword->{
            keywordTrackService.addTrackByKeyword(keyword.getId(),trackId);
            keywordTrackService.recommendTrack(keyword.getId(), trackId);
        });
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestTrackResponseDto.from(rt));
    }

    @PutMapping("/{requestId}/tracks/{trackId}/rating")
    public ResponseEntity<RequestTrackRatingResponseDto> rateRequestTrackRating(
            @PathVariable @NotNull @Positive Long userId,
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long trackId,
            @Valid @RequestBody RequestTrackRatingRequestDto ratingDto
            ){
        RequestTrack rt = ratingApplicationService.rateTrack(userId,requestId,trackId, ratingDto.getRating());
        return ResponseEntity.ok(RequestTrackRatingResponseDto.from(rt));
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
        // 키워드를 추가했을 때 키워드와 연결된 track을 해당 request track에 추가
//        List<Track> tracks = keywordTrackService.getTracksByKeyword(keywordId);
//
//        tracks.forEach(
//                track -> requestTrackService.addTrackByRequest(userId, requestId, track.getId()));

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

    @DeleteMapping("/{requestId}/keywords/{keywordId}")
    public ResponseEntity<Void> deleteKeywordByRequest(
            @PathVariable @NotNull @Positive Long requestId,
            @PathVariable @NotNull @Positive Long keywordId){
        requestKeywordService.deleteKeywordByRequestId(requestId,keywordId);
        return ResponseEntity.noContent().build();
    }
}
