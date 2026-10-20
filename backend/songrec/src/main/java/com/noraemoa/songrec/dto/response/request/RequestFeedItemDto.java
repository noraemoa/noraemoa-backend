package com.noraemoa.songrec.dto.response.request;

import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.dto.response.keyword.KeywordResponseDto;
import com.noraemoa.songrec.repository.projection.RequestKeywordRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class RequestFeedItemDto {

  private Long id;
  private String username;
  private String originalPrompt;
  private String title;
  private String thumbnailUrl;
  private List<String> keywords;
  private Integer trackCount; // RequestTrack
  private LocalDateTime createdAt;


  public static RequestFeedItemDto from(Request request, List<String> keywords,
      Integer trackCount) {

    return RequestFeedItemDto.builder()
        .id(request.getId())
        .username(request.getUser().getUsername())
        .title(request.getTitle())
        .thumbnailUrl(request.getThumbnailUrl())
        .keywords(keywords)
        .trackCount(trackCount)
        .createdAt(request.getCreatedAt())
        .build();
  }
}
