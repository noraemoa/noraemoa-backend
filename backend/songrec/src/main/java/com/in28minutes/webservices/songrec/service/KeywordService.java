package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.Keyword;
import com.in28minutes.webservices.songrec.dto.request.KeywordCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.response.KeywordResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeywordService {
    private final KeywordRepository keywordRepository;
    private String normalize(String s){
        String lowered = s.toLowerCase();
        String noSpaces = lowered.replaceAll("\\s+", "");
        return noSpaces.replaceAll("[\\p{Punct}]+", "");
    }

    @Transactional
    public Keyword createKeyword(KeywordCreateRequestDto dto){
        String raw = dto.getRawText().trim();
        String normalized = normalize(raw);

        return keywordRepository.findByNormalizedText(normalized)
                .orElseGet(()->keywordRepository.save(
                        Keyword.builder()
                                .rawText(raw)
                                .normalizedText(normalized)
                                .build()
                ));
    }

    @Transactional(readOnly = true)
    public Keyword getKeyword(Long keywordId){
        return keywordRepository.findById(keywordId)
                .orElseThrow(()->new NotFoundException("해당 키워드를 찾을 수 없습니다."));
    }
}
