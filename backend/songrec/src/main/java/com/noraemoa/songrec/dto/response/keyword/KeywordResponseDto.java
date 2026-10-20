package com.noraemoa.songrec.dto.response.keyword;

import com.noraemoa.songrec.domain.keyword.Keyword;
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
