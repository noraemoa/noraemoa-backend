package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.TrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackService {
    private final TrackRepository trackRepository;


    @Transactional
    public Track createTrack(TrackCreateRequestDto trackCreateRequestDto) {
        Track track = Track.builder()
                .spotifyId(trackCreateRequestDto.getSpotifyId())
                .name(trackCreateRequestDto.getName())
                .artist(trackCreateRequestDto.getArtist())
                .album(trackCreateRequestDto.getAlbum())
                .build();
        return trackRepository.findBySpotifyId(trackCreateRequestDto.getSpotifyId())
                .orElseGet(()->trackRepository.save(track));
    }

    public List<Track> getAllTracks() {
        return trackRepository.findAll();
    }

    public Track getTrackById(Long id) {
        return trackRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("해당 트랙을 찾을 수 없습니다."));
    }
}
