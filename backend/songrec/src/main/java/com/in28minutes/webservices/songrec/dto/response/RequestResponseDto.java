package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Request;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestResponseDto {
    private Long id;
    private Long userId;
    private String title;

    public static RequestResponseDto from(Request request){
        return RequestResponseDto.builder()
                .id(request.getId())
                .userId(request.getUserId())
                .title(request.getTitle())
                .build();
    }
}
