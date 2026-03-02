package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
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
