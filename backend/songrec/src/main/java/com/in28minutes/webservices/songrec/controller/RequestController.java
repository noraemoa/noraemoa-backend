package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.RequestResponseDto;
import com.in28minutes.webservices.songrec.dto.response.RequestTrackResponseDto;
import com.in28minutes.webservices.songrec.dto.response.TrackResponseDto;
import com.in28minutes.webservices.songrec.service.RequestService;
import com.in28minutes.webservices.songrec.service.RequestTrackService;
import com.in28minutes.webservices.songrec.service.TrackService;
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
    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<RequestResponseDto> createRequest(
            @Valid @RequestBody RequestCreateRequestDto requestDto,
            @PathVariable @NotNull @Positive Long userId) {
        Request request = requestService.createRequest(requestDto,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(RequestResponseDto.from(request));
    }

    @PatchMapping("/{requestId}")
    public RequestResponseDto updateRequest(@Valid @RequestBody RequestCreateRequestDto requestDto,
                                            @PathVariable @NotNull @Positive Long userId,
                                            @PathVariable @NotNull @Positive Long requestId) {
        Request request = requestService.updateRequest(requestDto,userId,requestId);
        return RequestResponseDto.from(request);
    }

    @GetMapping
    public List<RequestResponseDto> getRequests(@PathVariable @NotNull @Positive Long userId) {
        List<Request> requestList =  requestService.getRequestsByUserId(userId);
        return requestList
                .stream().map(RequestResponseDto::from)
                .toList();
    }

    @GetMapping("/{requestId}/tracks")
    public List<TrackResponseDto> getTracksByRequest(@PathVariable @NotNull @Positive Long userId, @PathVariable @NotNull @Positive Long requestId) {
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
}
