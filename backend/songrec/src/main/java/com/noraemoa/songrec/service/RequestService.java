package com.noraemoa.songrec.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.dto.request.RequestCreateRequestDto;
import com.noraemoa.songrec.dto.request.TrackSemanticSearchItemDto;
import com.noraemoa.songrec.dto.response.request.RequestResponseDto;
import com.noraemoa.songrec.global.exception.ExternalApiException;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.integration.openai.dto.RequestPromptRefineResult;
import com.noraemoa.songrec.repository.RequestRepository;
import com.noraemoa.songrec.repository.UserRepository;
import com.noraemoa.songrec.service.fileStorage.FileStorageService;
import com.noraemoa.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import com.noraemoa.songrec.service.qdrant.TrackSemanticSearchService;
import com.noraemoa.songrec.service.qdrant.TrackSemanticSearchService.SemanticSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestService {

  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;
  private final RequestPromptAiService requestPromptAiService;
  private final FileStorageService fileStorageService;
  private final TrackSemanticSearchService trackSemanticSearchService;
  private final RequestWriteService requestWriteService;

  @Transactional
  public Request updateRequest(RequestCreateRequestDto requestDto, Long userId, Long requestId) {
    Request request = getActiveRequest(userId, requestId);
    request.setTitle(requestDto.getPrompt());
    return request;
  }

  @Transactional(readOnly = true)
  public Request getActiveRequest(Long userId, Long requestId) {
    return requestRepository.findByIdAndUserIdAndDeletedFalse(requestId, userId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
  }

  @Transactional(readOnly = true)
  public Request getRequestFeed(Long requestId) {
    return requestRepository.findByIdAndDeletedFalse(requestId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
  }

  @Transactional(readOnly = true)
  public Page<Request> getAllRequests(int pageNumber, int pageSize) {
    Pageable pageable = PageRequest.of(pageNumber, pageSize);
    return requestRepository.findFeed(pageable);
  }

  @Transactional(readOnly = true)
  public List<Request> getRequestsByUserId(Long userId) {
    return requestRepository.findAllByUserIdAndDeletedFalse(userId);
  }

  @Transactional
  public void deleteRequest(Long userId, Long requestId) {
    Request request = getActiveRequest(userId, requestId);
    request.setDeleted(true);
  }

  //Admin 사용자만 모든 request를 삭제할 수 있음.
  @Transactional
  public void deleteRequestAdmin(Long requestId) {
    Request request = getRequestFeed(requestId);
    request.setDeleted(true);
  }

  @Transactional
  public Request uploadThumbnail(Long userId, Long requestId, MultipartFile file)
      throws IOException {
    Request request = getActiveRequest(userId, requestId);

    StoredFile stored =
        fileStorageService.storeRequestThumbnail(requestId, file);

    request.setThumbnailKey(stored.key());
    request.setThumbnailUrl(stored.url());
    return request;
  }

  private String fallbackTitle(String prompt) {
    if (prompt == null || prompt.isBlank()) {
      return "My Playlist";
    }

    String normalized = prompt.trim();

    if (normalized.length() <= 30) {
      return normalized;
    }

    return normalized.substring(0, 30);
  }

  public List<String> readKeywords(String keywordsJson) {
    try {
      return objectMapper.readValue(
          keywordsJson,
          new TypeReference<List<String>>() {
          }
      );
    } catch (Exception e) {
      return List.of();
    }
  }


  /**
   * 추천 요청 생성. 이 메서드에는 일부러 @Transactional 을 붙이지 않는다.
   *
   * 1) 외부 API 호출(프롬프트 정제, 질의 분석, 임베딩, Qdrant 검색)은 트랜잭션 밖에서 한다.
   *    → 수 초 걸리는 OpenAI 응답을 기다리는 동안 DB 커넥션을 붙잡지 않는다.
   * 2) DB 쓰기(Keyword, Request, RequestTrack)는 RequestWriteService 의 짧은 트랜잭션 1개로 묶는다.
   *    → 외부 API 가 실패하면 그 전에 예외가 나므로 DB 에 반쯤 저장된 데이터가 남지 않는다.
   * 3) Qdrant 질의 벡터 저장은 커밋 이후에 한다. 실패해도 추천 결과에는 영향이 없다.
   * 4) 썸네일은 커밋 후 이벤트로 비동기 생성한다(RequestThumbnailEventListener). 응답의 thumbnailUrl 은 null 이다.
   */
  public RequestResponseDto createRequest(RequestCreateRequestDto dto, Long userId) {
    // 외부 API 비용을 쓰기 전에 사용자 존재 여부만 빠르게 확인
    if (!userRepository.existsById(userId)) {
      throw new NotFoundException("User not found");
    }

    // 1) 프롬프트 정제(OpenAI) - 실패해도 fallback 제목으로 계속 진행
    RequestPromptRefineResult refineResult;
    try {
      refineResult = requestPromptAiService.refine(dto.getPrompt());
    } catch (Exception e) {
      log.warn("Prompt refine failed, using fallback title. userId={}", userId, e);
      refineResult = new RequestPromptRefineResult(fallbackTitle(dto.getPrompt()), List.of());
    }

    // 2) 트랙 검색(OpenAI + Qdrant) - 추천의 핵심이므로 실패하면 아무것도 저장하지 않고 실패 응답
    SemanticSearchResult searchResult;
    try {
      searchResult = trackSemanticSearchService.search(userId, dto.getPrompt(), 5);
    } catch (Exception e) {
      log.error("Track semantic search failed. userId={}", userId, e);
      throw new ExternalApiException("추천 곡 검색에 실패했습니다. 잠시 후 다시 시도해 주세요.", e);
    }
    List<Long> trackIds = searchResult.items().stream()
        .map(TrackSemanticSearchItemDto::getTrackId).toList();

    // 3) DB 쓰기 - 짧은 트랜잭션 1개 (전부 저장되거나 전부 롤백)
    RequestResponseDto response =
        requestWriteService.saveRequest(dto, userId, refineResult, trackIds);

    // 4) 커밋 후 Qdrant 질의 벡터 저장 - 실패해도 추천 결과는 그대로 반환
    try {
      long t = System.nanoTime();
      trackSemanticSearchService.upsertQueryPoint(
          response.getId(), searchResult.queryVector(), searchResult.searchText());
      log.info("QDRANT_QUERY_UPSERT requestId={} ms={}", response.getId(),
          (System.nanoTime() - t) / 1_000_000);
    } catch (Exception e) {
      log.warn("Query vector upsert failed. requestId={}", response.getId(), e);
    }

    return response;
  }
}
