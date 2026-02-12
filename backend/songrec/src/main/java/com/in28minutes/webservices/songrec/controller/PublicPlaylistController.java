package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Playlist;
import com.in28minutes.webservices.songrec.dto.response.PlaylistResponseDto;
import com.in28minutes.webservices.songrec.service.PlaylistService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/playlists")
public class PublicPlaylistController {

    private final PlaylistService playlistService;

    @GetMapping("/{playlistId}")
    public PlaylistResponseDto getPublicPlaylist(@PathVariable @NotNull @Positive Long playlistId){
        Playlist playlist = playlistService.getPublicPlaylist(playlistId);
        return PlaylistResponseDto.from(playlist);
    }

    @GetMapping
    public Page<PlaylistResponseDto> getPublicPlaylists(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size
    ){
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "id"));
        return playlistService.getPublicPlaylists(pageable).map(PlaylistResponseDto::from);
    }
}
