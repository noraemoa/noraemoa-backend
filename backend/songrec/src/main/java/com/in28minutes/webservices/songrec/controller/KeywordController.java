package com.in28minutes.webservices.songrec.controller;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.domain.KeywordTrack;
import com.in28minutes.webservices.songrec.domain.Track;
import com.in28minutes.webservices.songrec.dto.request.KeywordCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.KeywordResponseDto;
import com.in28minutes.webservices.songrec.dto.response.KeywordTrackResponseDto;
import com.in28minutes.webservices.songrec.dto.response.TrackResponseDto;
import com.in28minutes.webservices.songrec.service.KeywordService;
import com.in28minutes.webservices.songrec.service.KeywordTrackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class KeywordController {
    private final KeywordService keywordService;
    private final KeywordTrackService keywordTrackService;

    @PostMapping("/keywords")
    public ResponseEntity<KeywordResponseDto> createKeyword(@Valid @RequestBody KeywordCreateRequestDto dto){
        KeywordResponseDto keyword =  KeywordResponseDto.from(keywordService.createKeyword(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(keyword);
    }

    @GetMapping("/keywords/{keywordId}")
    public KeywordResponseDto getKeyword(@PathVariable Long keywordId){
        return KeywordResponseDto.from(keywordService.getKeyword(keywordId));
    }

    @GetMapping("/keywords")
    public List<KeywordResponseDto> getAllKeywords(){
        List<Keyword> keywords = keywordService.getAllKeywords();
        return keywords.stream().map(KeywordResponseDto::from).toList();
    }

    @PostMapping("/keywords/{keywordId}/tracks/{trackId}")
    public ResponseEntity<KeywordTrackResponseDto> addTrackByKeyword(
            @PathVariable @NotNull @Positive Long keywordId,
            @PathVariable @NotNull @Positive Long trackId){
        keywordTrackService.addTrackByKeyword(keywordId, trackId);
        KeywordTrackResponseDto keywordTrack = KeywordTrackResponseDto.from(
                keywordTrackService.recommendTrack(keywordId,trackId));

        return ResponseEntity.status(HttpStatus.CREATED).body(keywordTrack);
    }

    @GetMapping ("/keywords/{keywordId}/tracks")
    public List<TrackResponseDto> getTrackByKeyword(@PathVariable Long keywordId){
        List<Track> tracksList = keywordTrackService.getTracksByKeyword(keywordId);
        return tracksList.stream().map(TrackResponseDto::from).toList();
    }

    @GetMapping("/keywords/{keywordId}/tracks/{trackId}/recommend")
    public KeywordTrackResponseDto getTrackRecommendCount(
            @PathVariable @NotNull @Positive Long keywordId,
            @PathVariable @NotNull @Positive Long trackId
    ){
        KeywordTrack kt = keywordTrackService.getKeywordTrack(keywordId,trackId);
        return KeywordTrackResponseDto.from(kt);
    }
}
