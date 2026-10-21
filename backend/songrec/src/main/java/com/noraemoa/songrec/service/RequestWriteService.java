package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.keyword.Keyword;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.domain.user.User;
import com.noraemoa.songrec.dto.request.KeywordCreateRequestDto;
import com.noraemoa.songrec.dto.request.RequestCreateRequestDto;
import com.noraemoa.songrec.dto.response.request.RequestResponseDto;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.service.event.RequestCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 요청 생성 과정의 DB 쓰기만 담당하는 서비스.
 *
 * 스프링의 @Transactional 은 빈을 감싼 프록시가 메서드 호출을 가로채서 트랜잭션을 연다.
 * RequestService 안에서 같은 클래스의 @Transactional 메서드를 this.save() 처럼 부르면
 * 프록시를 거치지 않으므로 트랜잭션이 열리지 않는다(self-invocation).
 * 그래서 DB 쓰기를 별도 빈으로 분리해, RequestService 가 프록시를 통해 호출하게 했다.
 */
@Service
@RequiredArgsConstructor
public class RequestWriteService {

  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final KeywordService keywordService;
  private final RequestTrackService requestTrackService;
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Keyword, Request, RequestTrack 을 하나의 트랜잭션으로 저장한다. 중간에 실패하면 전부 롤백된다.
   * 외부 API 호출이 모두 끝난 뒤에만 호출되므로 이 트랜잭션은 DB 작업 시간만큼만 커넥션을 쓴다.
   * 발행한 RequestCreatedEvent 는 커밋된 뒤에만 리스너(썸네일 생성)에 전달된다.
   */
  @Transactional
  public RequestResponseDto saveRequest(RequestCreateRequestDto dto, Long userId,
      RequestPromptRefineResult refineResult, List<Long> trackIds) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));

    List<Keyword> keywords = refineResult.getKeywords().stream()
        .map(k -> keywordService.createKeyword(new KeywordCreateRequestDto(k))).toList();
    List<String> rawKeywords = keywords.stream().map(Keyword::getRawText).toList();

    Request request = Request.builder()
        .user(user)
        .originalPrompt(dto.getPrompt())
        .title(refineResult.getTitle())
        .promptKeywordsJson(writeKeywords(rawKeywords))
        .deleted(false)
        .build();
    requestRepository.saveAndFlush(request);

    trackIds.forEach(trackId -> requestTrackService.addTrackByRequest(request.getId(), trackId));

    eventPublisher.publishEvent(new RequestCreatedEvent(
        request.getId(), dto.getPrompt(), refineResult.getTitle(), rawKeywords));

    // 응답 DTO 변환까지 트랜잭션 안에서 처리 (OSIV 없이도 LAZY 필드 접근 가능)
    return RequestResponseDto.from(request, keywords);
  }

  @Transactional
  public void updateThumbnail(Long requestId, String key, String url) {
    Request request = requestRepository.findById(requestId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
    request.setThumbnailKey(key);
    request.setThumbnailUrl(url);
  }

  private String writeKeywords(List<String> keywords) {
    try {
      return objectMapper.writeValueAsString(keywords);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Failed to serialize keywords", e);
    }
  }
}
