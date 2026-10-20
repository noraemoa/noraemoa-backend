package com.noraemoa.songrec.dto.response.request;

import com.noraemoa.songrec.domain.request.Request;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestSummaryResponseDto {
    private Long id;
    private Long userId;
    private String title;
    private String thumbnailUrl;

    public static RequestSummaryResponseDto from(Request request){
        return RequestSummaryResponseDto.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .title(request.getTitle())
                .thumbnailUrl(request.getThumbnailUrl())
                .build();
    }
}
