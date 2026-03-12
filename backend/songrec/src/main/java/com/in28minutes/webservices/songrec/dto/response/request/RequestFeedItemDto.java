package com.in28minutes.webservices.songrec.dto.response.request;

import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.dto.response.keyword.KeywordResponseDto;
import com.in28minutes.webservices.songrec.repository.projection.RequestKeywordRow;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class RequestFeedItemDto {

  private Long id; //Request
  private String username; // Request
  private String title; // Request
  private String thumbnailUrl; // Request
  private List<KeywordResponseDto> keywords; // RequestKeyword
  private Integer trackCount; // RequestTrack
  private LocalDateTime createdAt; // Request

  public static RequestFeedItemDto from(Request request, List<RequestKeywordRow> keywordRows,
      Integer trackCount) {
    List<KeywordResponseDto> keywords = keywordRows.stream().map(
            kr -> KeywordResponseDto.builder().id(kr.getKeywordId()).rawText(kr.getRawText()).build())
        .toList();
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
