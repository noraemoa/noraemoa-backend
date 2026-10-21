package com.noraemoa.songrec.service.event;

import java.util.List;

/**
 * 추천 요청이 저장됐다는 이벤트. 트랜잭션이 커밋된 뒤 썸네일 생성을 시작하는 데 쓴다.
 * 엔티티 대신 값만 담아서, 다른 스레드에서 LAZY 로딩 문제 없이 쓸 수 있게 했다.
 */
public record RequestCreatedEvent(Long requestId, String prompt, String title,
                                  List<String> keywords) {
}
