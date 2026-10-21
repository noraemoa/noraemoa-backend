package com.noraemoa.songrec.service;

import com.noraemoa.songrec.domain.request.Request;
import com.noraemoa.songrec.global.exception.NotFoundException;
import com.noraemoa.songrec.repository.RequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 요청(Request)의 소유권 확인을 한곳에 모은 서비스.
 *
 * 요청을 수정하는 API 는 모두 여기의 getOwnedRequest() 로 "로그인한 사용자의 요청인지"를 확인한다.
 * 소유자가 아니거나 삭제된 요청이면 404 를 던진다. 403 대신 404 를 쓰는 이유는,
 * 남의 요청 ID 를 바꿔 가며 호출했을 때 그 요청이 존재하는지조차 알려 주지 않기 위해서다.
 *
 * RequestService 가 아니라 별도 빈으로 둔 이유: RequestTrackService 가 RequestService 를 의존하면
 * RequestService → RequestWriteService → RequestTrackService → RequestService 순환 참조가 생긴다.
 */
@Service
@RequiredArgsConstructor
public class RequestOwnershipService {

  private final RequestRepository requestRepository;

  @Transactional(readOnly = true)
  public Request getOwnedRequest(Long userId, Long requestId) {
    return requestRepository.findByIdAndUserIdAndDeletedFalse(requestId, userId)
        .orElseThrow(() -> new NotFoundException("해당 요청을 찾을 수 없습니다."));
  }
}
