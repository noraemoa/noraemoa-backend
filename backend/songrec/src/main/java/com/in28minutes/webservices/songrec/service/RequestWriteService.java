package com.in28minutes.webservices.songrec.service;

import com.in28minutes.webservices.songrec.domain.keyword.Keyword;
import com.in28minutes.webservices.songrec.domain.request.Request;
import com.in28minutes.webservices.songrec.domain.user.User;
import com.in28minutes.webservices.songrec.dto.request.KeywordCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.RequestCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.TrackSemanticSearchItemDto;
import com.in28minutes.webservices.songrec.dto.response.request.RequestResponseDto;
import com.in28minutes.webservices.songrec.global.exception.NotFoundException;
import com.in28minutes.webservices.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.in28minutes.webservices.songrec.repository.RequestRepository;
import com.in28minutes.webservices.songrec.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 요청 생성 과정의 DB 쓰기만 담당하는 서비스.
 *
 * RequestService.createRequest()에서 같은 클래스의 @Transactional 메서드를 this로 호출하면
 * 스프링 프록시를 거치지 않아 트랜잭션이 적용되지 않는다(self-invocation).
 * 그래서 DB 쓰기 메서드를 별도 빈으로 분리해, 외부에서 프록시를 통해 호출되도록 했다.
 */
@Service
@RequiredArgsConstructor
public class RequestWriteService {

  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final KeywordService keywordService;
  private final RequestTrackService requestTrackService;
  private final ObjectMapper objectMapper;

  public record RequestCreationResult(Request request, List<Keyword> keywords) {
  }

  // 키워드 저장 + Request 저장을 하나의 트랜잭션으로 묶음 (중간 실패 시 함께 롤백)
  @Transactional
  public RequestCreationResult createRequestWithKeywords(
      RequestCreateRequestDto dto, Long userId, RequestPromptRefineResult refineResult) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    List<Keyword> keywords = refineResult.getKeywords().stream()
        .map(k -> keywordService.createKeyword(new KeywordCreateRequestDto(k))).toList();

    Request request = Request.builder()
        .user(user)
        .originalPrompt(dto.getPrompt())
        .title(refineResult.getTitle())
        .promptKeywordsJson(writeKeywords(keywords.stream().map(Keyword::getRawText).toList()))
        .deleted(false)
        .build();
    requestRepository.saveAndFlush(request);
    return new RequestCreationResult(request, keywords);
  }

  // 추천곡 매핑 저장을 하나의 트랜잭션으로 묶음
  @Transactional
  public void addTracksToRequest(Long requestId, List<TrackSemanticSearchItemDto> searchResults) {
    searchResults.forEach(r -> requestTrackService.addTrackByRequest(requestId, r.getTrackId()));
  }

  @Transactional
  public void updateThumbnail(Long requestId, String key, String url) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
    request.setThumbnailKey(key);
    request.setThumbnailUrl(url);
  }

  // 응답 DTO 변환까지 트랜잭션 안에서 처리 (OSIV 없이도 LAZY 필드 접근 가능)
  @Transactional(readOnly = true)
  public RequestResponseDto getRequestResponse(Long requestId, List<Keyword> keywords) {
    Request request = requestRepository.findByIdAndDeletedFalse(requestId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
    return RequestResponseDto.from(request, keywords);
  }

  private String writeKeywords(List<String> keywords) {
    try {
      return objectMapper.writeValueAsString(keywords);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize keywords", e);
    }
  }
}