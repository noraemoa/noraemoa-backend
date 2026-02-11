package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.*;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.PlaylistTrackRepository;
import com.in28minutes.webservices.songrec.repository.RequestTrackRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaylistTrackService {
    private final PlaylistService playlistService;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final TrackService trackService;
    private final RequestTrackRepository requestTrackRepository;

    @Transactional
    public PlaylistTrack getActivePlaylistTrack(Long userId, Long playlistId, Long trackId) {
        playlistService.getActivePlaylist(userId, playlistId);
        return playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId,trackId)
                .orElseThrow(()->new NotFoundException("PlaylistTrack not found"));
    }

    @Transactional(readOnly = true)
    public List<Track> getTracksByPlaylist(Long userId, Long playlistId) {
        playlistService.getActivePlaylist(userId, playlistId); //userId 검증용

        return playlistTrackRepository.findActiveTracksByPlaylistId(playlistId);
    }

    @Transactional
    public PlaylistTrack addTrackByPlaylist(Long userId, Long playlistId, Long trackId) {
        Playlist playlist = playlistService.getActivePlaylist(userId, playlistId);
        Track track = trackService.getTrack(trackId);

        return playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId, trackId)
                .map(existing-> {
                    if(Boolean.TRUE.equals(existing.getTrackDeleted()))
                        existing.setTrackDeleted(false); return existing;})
                .orElseGet(()-> playlistTrackRepository.save(
                        PlaylistTrack.builder()
                                .playlist(playlist)
                                .track(track)
                                .trackDeleted(false).build()));
    }

    @Transactional
    public void deleteTrack(Long userId,Long playlistId, Long trackId) {
        playlistService.getActivePlaylist(userId, playlistId); //userId 검증용

        PlaylistTrack pt = playlistTrackRepository.findByPlaylist_IdAndTrack_Id(playlistId,trackId)
                .orElseThrow(()->new NotFoundException("PlaylistTrack not found"));
        pt.setTrackDeleted(true);
    }
}
