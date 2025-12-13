package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.RequestResponseDto;
import com.in28minutes.webservices.songrec.service.RequestService;
import com.in28minutes.webservices.songrec.service.RequestTrackService;
import com.in28minutes.webservices.songrec.service.TrackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class RequestController {
    private final RequestService requestService;
    private final RequestTrackService requestTrackService;
    private final TrackService trackService;

    @PostMapping("/users/{userId}/requests")
    public RequestResponseDto createRequest(@Valid @RequestBody RequestCreateRequestDto requestDto, @PathVariable("userId") @NotNull @Positive Long userId) {
        Request request = requestService.createRequest(requestDto,userId);
        return RequestResponseDto.from(request);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}")
    public RequestResponseDto updateRequest(@Valid @RequestBody RequestCreateRequestDto requestDto,
                                            @PathVariable("userId") @NotNull @Positive Long userId,
                                            @PathVariable("requestId") @NotNull @Positive Long requestId) {
        Request request = requestService.updateRequest(requestDto,userId,requestId);
        return RequestResponseDto.from(request);
    }

    @GetMapping("/users/{userId}/requests")
    public List<Request> getRequests(@PathVariable("userId") Long userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @GetMapping("/requests/{requestId}/tracks")
    public List<Track> getTracksByRequest(@PathVariable("requestId") Long requestId) {
        return requestTrackService.getTracksByRequest(requestId);
    }

    @PostMapping("/requests/{requestId}/tracks/{trackId}")
    public RequestTrack addTrackByRequest(@PathVariable("requestId") Long requestId,@PathVariable("trackId") Long trackId) {
        Request request = requestService.getRequestById(requestId);
        Track track = trackService.getTrackById(trackId);
        return requestTrackService.addTrackByRequest(request,track);
    }
}
