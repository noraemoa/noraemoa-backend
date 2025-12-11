package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.service.TrackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TrackController {
    private final TrackService trackService;

    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping("/tracks")
    public Track track(@RequestBody Track track) {
        return trackService.createTrack(track);
    }

    @GetMapping("/tracks")
    public List<Track> track() {
        return trackService.getAllTracks();
    }
}
