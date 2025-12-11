package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTrackService {
    private final RequestTrackRepository requestTrackRepository;

    public List<Track> getTracksByRequest(Long requestId) {
        return requestTrackRepository.findTracksByRequestId(requestId);
    }

    public RequestTrack addTrackByRequest(Request request, Track track) {
        RequestTrack requestTrack = new RequestTrack();
        requestTrack.setRequest(request);
        requestTrack.setTrack(track);
        return requestTrackRepository.save(requestTrack);
    }
}
