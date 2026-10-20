package com.noraemoa.songrec.dto.response.request;

import com.noraemoa.songrec.domain.request.RequestKeyword;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestKeywordResponseDto {
    private Long id;
    private Long requestId;
    private Long keywordId;

    public static RequestKeywordResponseDto from(RequestKeyword requestKeyword){
        return RequestKeywordResponseDto.builder()
                .id(requestKeyword.getId())
                .requestId(requestKeyword.getRequest().getId())
                .keywordId(requestKeyword.getKeyword().getId())
                .build();
    }
}
