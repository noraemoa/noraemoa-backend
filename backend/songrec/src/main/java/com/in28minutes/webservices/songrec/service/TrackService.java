package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackService {
    private final TrackRepository trackRepository;

    public TrackService(TrackRepository trackRepository) {
        this.trackRepository = trackRepository;
    }

    public Track createTrack(Track track) {
        return trackRepository.save(track);
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackById(Long id) {
        return trackRepository.findById(id).orElse(null);
    }
}
