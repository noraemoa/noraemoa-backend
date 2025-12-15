package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Request;
import com.in28minutes.webservices.songrec.domain.RequestTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTrackService {
    private final RequestTrackRepository requestTrackRepository;
    private final TrackService trackService;
    private final RequestService requestService;

    @Transactional(readOnly = true)
    public List<Track> getTracksByRequest(Long userId, Long requestId) {
        requestService.getActiveRequest(userId, requestId); //userId 검증용

        return requestTrackRepository.findActiveTracksByRequestId(requestId);
    }

    @Transactional
    public RequestTrack addTrackByRequest(Long userId, Long requestId, Long trackId) {
        Request request = requestService.getActiveRequest(userId, requestId);
        Track track = trackService.getTrackById(trackId);

        RequestTrack requestTrack = RequestTrack.builder()
                                .request(request)
                                .track(track).build();
        return requestTrackRepository.save(requestTrack);
    }

    @Transactional
    public void deleteTrack(Long userId,Long requestId, Long trackId) {
        requestService.getActiveRequest(userId, requestId); //userId 검증용

        RequestTrack rt = requestTrackRepository.findByRequest_IdAndTrack_Id(requestId,trackId)
                .orElseThrow(()->new NotFoundException("RequestTrack not found"));
        rt.setTrackDeleted(true);
    }
}
