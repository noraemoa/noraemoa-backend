package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Keyword;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KeywordResponseDto {
    private Long id;
    private String rawText;

    public static KeywordResponseDto from(Keyword keyword) {
        return KeywordResponseDto.builder()
                .id(keyword.getId())
                .rawText(keyword.getRawText())
                .build();
    }
}
