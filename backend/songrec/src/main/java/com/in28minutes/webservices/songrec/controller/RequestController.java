package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.service.RequestService;
import com.in28minutes.webservices.songrec.service.RequestTrackService;
import com.in28minutes.webservices.songrec.service.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;
    private final RequestTrackService requestTrackService;
    private final TrackService trackService;

    @PostMapping("/requests")
    public Request createRequest(@RequestBody Request request) {
        return requestService.createRequest(request);
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
