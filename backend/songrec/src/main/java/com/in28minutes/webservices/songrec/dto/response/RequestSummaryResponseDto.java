package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.Request;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestSummaryResponseDto {
    private Long id;
    private Long userId;
    private String title;

    public static RequestSummaryResponseDto from(Request request){
        return RequestSummaryResponseDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .title(request.getTitle())
                .build();
    }
}
