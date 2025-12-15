package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.global.exception.ConflictException;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTrackService {
    private final RequestTrackRepository requestTrackRepository;
    private final TrackService trackService;
    private final RequestService requestService;

    public List<Track> getTracksByRequest(Long userId, Long requestId) {
        requestService.getRequestByUserIdAndRequestId(userId, requestId); //userId 검증용

        return requestTrackRepository.findTracksByRequestId(requestId);
    }

    public RequestTrack addTrackByRequest(Long userId, Long requestId, Long trackId) {
        Request request = requestService.getRequestByUserIdAndRequestId(userId, requestId);
        Track track = trackService.getTrackById(trackId);

        RequestTrack requestTrack = RequestTrack.builder()
                                .request(request)
                                .track(track).build();
        return requestTrackRepository.save(requestTrack);
    }
}
