package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.application.RatingApplicationService;
import com.in28minutes.webservices.songrec.config.security.JwtPrincipal;
import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.request.RequestKeyword;
import com.in28minutes.webservices.songrec.domain.request.RequestTrack;
import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.RequestTrackRatingRequestDto;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.*;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTrackResponseDto;
import com.in28minutes.webservices.songrec.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/requests")
public class RequestController {

  private final RequestService requestService;
  private final RequestTrackService requestTrackService;
  private final RequestKeywordService requestKeywordService;
  private final KeywordTrackService keywordTrackService;
  private final RatingApplicationService ratingApplicationService;
  private final RequestFeedService requestFeedService;

  // requests
  @PostMapping
  public ResponseEntity<RequestResponseDto> createRequest(
      @Valid @RequestBody RequestCreateRequestDto requestDto,
      @AuthenticationPrincipal JwtPrincipal principal) {
    Request request = requestService.createRequest(requestDto, principal.userId());
    List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(request.getId())
        .stream().map(KeywordResponseDto::from).toList();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(RequestResponseDto.from(request, keywords));
  }

  @PatchMapping("/{requestId}")
  public RequestResponseDto updateRequest(@Valid @RequestBody RequestCreateRequestDto requestDto,
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId) {
    Request request = requestService.updateRequest(requestDto, principal.userId(), requestId);
    List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(requestId)
        .stream().map(KeywordResponseDto::from).toList();
    return RequestResponseDto.from(request, keywords);
  }

  @GetMapping("/feed")
  public List<RequestFeedItemDto> getFeed(@RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    return requestFeedService.getFeed(page, size);
  }

  @GetMapping("/me")
  public List<RequestSummaryResponseDto> getMyRequests(
      @AuthenticationPrincipal JwtPrincipal principal) {
    List<Request> requestList = requestService.getRequestsByUserId(principal.userId());

    return requestList.stream().map(RequestSummaryResponseDto::from).toList();
  }

  @GetMapping("/me/{requestId}")
  public RequestResponseDto getMyRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId) {

    Request request = requestService.getActiveRequest(principal.userId(), requestId);
    List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(request.getId())
        .stream().map(KeywordResponseDto::from).toList();

    return RequestResponseDto.from(request, keywords);
  }

  @GetMapping("/{requestId}")
  public RequestResponseDto getRequestFeed(
      @PathVariable @NotNull @Positive Long requestId) {

    Request request = requestService.getRequestFeed(requestId);
    List<KeywordResponseDto> keywords = requestKeywordService.getKeywordsByRequest(request.getId())
        .stream().map(KeywordResponseDto::from).toList();

    return RequestResponseDto.from(request, keywords);
  }

  @DeleteMapping("/{requestId}")
  public ResponseEntity<Void> deleteRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId) {
    requestService.deleteRequest(principal.userId(), requestId);
    return ResponseEntity.noContent().build();
  }

  // tracks
  @GetMapping("/{requestId}/tracks")
  public List<TrackResponseDto> getTracksByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId) {
    return requestTrackService.getTracksByRequest(principal.userId(), requestId);
  }


   //키워드에 해당하는 트랙들을 받아서 추천도 순으로 정렬한 후 3개씩 받아와서 요청에 해당 트랙들 저장
  @PostMapping("/{requestId}/tracks/recommendations")
  public ResponseEntity<RecommendedTracksResponseDto> addRecommendedTracksToRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @RequestParam(defaultValue = "0") @PositiveOrZero int page,
      @RequestParam(defaultValue = "3") @Positive int size
  ) {
    Slice<Track> slice = keywordTrackService.getRecommendedTracks(requestId, page, size);
    List<Track> tracks = slice.getContent();
    tracks.forEach(track -> requestTrackService.addTrackByRequest(requestId, track.getId()));
    return ResponseEntity.status(HttpStatus.CREATED).body(
        RecommendedTracksResponseDto.builder()
            .tracks(tracks.stream().map(TrackSimpleResponseDto::from).toList())
            .hasNext(slice.hasNext())
            .nextPage(slice.hasNext() ? page + 1 : page)
            .build()
    );
  }

  // track에 추가되지 않은 spotify track을 track테이블에 먼저 추가하고 플리에 해당 track 저장
  @PostMapping("/{requestId}/tracks")
  public ResponseEntity<RequestTrackResponseDto> addSpotifyTrackByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @RequestBody @Valid TrackCreateRequestDto dto
  ) {
    RequestTrack rt = requestTrackService.addSpotifyTrackToRequest(requestId, dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(RequestTrackResponseDto.from(rt));
  }

  //요청에 트랙 직접 추가
  // track 테이블에 있는 노래만 이 api 쓸 수 있음.
  @PostMapping("/{requestId}/tracks/{trackId}")
  public ResponseEntity<RequestTrackResponseDto> addTrackByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @PathVariable @NotNull @Positive Long trackId) {
    RequestTrack rt = requestTrackService.addTrackByRequest(requestId, trackId);

    // 해당 request의 keyword들과 이 track을 각각 연결 시키기 (해당 track을 고용 바구니에도 추가하는 작업)
    List<Keyword> keywords = requestKeywordService.getKeywordsByRequest(requestId);
    keywords.forEach(keyword -> {
      keywordTrackService.addTrackByKeyword(keyword.getId(), trackId);
      keywordTrackService.recommendTrack(keyword.getId(), trackId);
    });
    return ResponseEntity.status(HttpStatus.CREATED).body(RequestTrackResponseDto.from(rt));
  }

  @PutMapping("/{requestId}/tracks/{trackId}/rating")
  public ResponseEntity<RequestTrackRatingResponseDto> rateRequestTrackRating(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @PathVariable @NotNull @Positive Long trackId,
      @Valid @RequestBody RequestTrackRatingRequestDto ratingDto
  ) {
    RequestTrack rt = ratingApplicationService.rateTrack(principal.userId(), requestId, trackId,
        ratingDto.getRating());
    return ResponseEntity.ok(RequestTrackRatingResponseDto.from(rt));
  }

  @DeleteMapping("/{requestId}/tracks/{trackId}")
  public ResponseEntity<Void> deleteTrackByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @PathVariable @NotNull @Positive Long trackId) {

    requestTrackService.deleteTrack(principal.userId(), requestId, trackId);
    return ResponseEntity.noContent().build();
  }

  //keywords
  @PostMapping("/{requestId}/keywords/{keywordId}")
  public ResponseEntity<RequestKeywordResponseDto> addKeywordByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @PathVariable @NotNull @Positive Long keywordId) {
    RequestKeyword rk = requestKeywordService.addKeywordByRequest(principal.userId(), requestId,
        keywordId);
    // 키워드를 추가했을 때 키워드와 연결된 track을 해당 request track에 추가
//        List<Track> tracks = keywordTrackService.getTracksByKeyword(keywordId);
//
//        tracks.forEach(
//                track -> requestTrackService.addTrackByRequest(userId, requestId, track.getId()));

    return ResponseEntity.status(HttpStatus.CREATED).body(RequestKeywordResponseDto.from(rk));
  }

  @GetMapping("/{requestId}/keywords")
  public List<KeywordResponseDto> getKeywordsByRequest(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId) {
    List<Keyword> keywordsList = requestKeywordService.getKeywordsByRequest(requestId);
    return keywordsList
        .stream().map(KeywordResponseDto::from)
        .toList();
  }

  @DeleteMapping("/{requestId}/keywords/{keywordId}")
  public ResponseEntity<Void> deleteKeywordByRequest(
      @PathVariable @NotNull @Positive Long requestId,
      @PathVariable @NotNull @Positive Long keywordId) {
    requestKeywordService.deleteKeywordByRequestId(requestId, keywordId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(value = "/{requestId}/thumbnail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<RequestSummaryResponseDto> uploadThumbnail(
      @AuthenticationPrincipal JwtPrincipal principal,
      @PathVariable @NotNull @Positive Long requestId,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    Request request = requestService.uploadThumbnail(principal.userId(), requestId, file);
    return ResponseEntity.ok(RequestSummaryResponseDto.from(request));
  }
}
