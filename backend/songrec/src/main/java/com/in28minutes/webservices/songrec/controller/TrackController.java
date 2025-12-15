package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.TrackResponseDto;
import com.in28minutes.webservices.songrec.service.TrackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<TrackResponseDto> createTrack(@Valid @RequestBody TrackCreateRequestDto trackCreateRequestDto) {

        Track track = trackService.createTrack(trackCreateRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(TrackResponseDto.from(track));
    }

    @GetMapping("/tracks")
    public List<TrackResponseDto> GetTrack() {

        List<Track> trackList = trackService.getAllTracks();
        return trackList.stream().map(TrackResponseDto::from).toList();
    }
}
