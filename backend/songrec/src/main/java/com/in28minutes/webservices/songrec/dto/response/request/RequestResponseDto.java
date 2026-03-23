package com.in28minutes.webservices.songrec.dto.response.request;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.dto.response.keyword.KeywordResponseDto;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class RequestResponseDto {

  private Long id;
  private Long userId;
  private String username;
  private String originalPrompt;
  private String title;
  private String thumbnailUrl;
  private List<String> keywords;
  private LocalDateTime createdAt;

  public static RequestResponseDto from(Request request, List<Keyword> keywords) {
    return RequestResponseDto.builder()
        .id(request.getId())
        .userId(request.getUser().getId())
        .username(request.getUser().getUsername())
        .originalPrompt(request.getOriginalPrompt())
        .title(request.getTitle())
        .thumbnailUrl(request.getThumbnailUrl())
        .keywords(keywords.stream().map(Keyword::getNormalizedText).toList())
        .createdAt(request.getCreatedAt()).build();
  }
}
