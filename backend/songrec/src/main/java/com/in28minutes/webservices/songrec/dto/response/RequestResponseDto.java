package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Request;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RequestResponseDto {
    private Long id;
    private Long userId;
    private String title;
    private List<KeywordResponseDto> keywords;

    public static RequestResponseDto from(Request request,List<KeywordResponseDto> keywords){
        return RequestResponseDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .title(request.getTitle())
                .keywords(keywords)
                .build();
    }
}
