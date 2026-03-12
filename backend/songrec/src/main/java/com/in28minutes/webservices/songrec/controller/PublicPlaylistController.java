package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.dto.response.playlist.PlaylistResponseDto;
import com.in28minutes.webservices.songrec.service.PlaylistService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/public/playlists")
public class PublicPlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    public Page<PlaylistResponseDto> getPublicPlaylists(
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Positive int size
    ){
        Pageable pageable = PageRequest.of(page,size, Sort.by(Sort.Direction.DESC, "id"));
        return playlistService.getPublicPlaylists(pageable).map(PlaylistResponseDto::from);
    }
}
