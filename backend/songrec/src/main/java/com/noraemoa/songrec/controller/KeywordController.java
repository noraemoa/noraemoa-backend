package com.noraemoa.songrec.controller;

import com.noraemoa.songrec.domain.keyword.Keyword;
import com.noraemoa.songrec.dto.request.KeywordCreateRequestDto;
import com.noraemoa.songrec.dto.response.keyword.KeywordResponseDto;
import com.noraemoa.songrec.service.KeywordService;
import jakarta.validation.Valid;
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


}
