package com.noraemoa.songrec.service;

import com.noraemoa.songrec.config.AsyncConfig;
import com.noraemoa.songrec.service.event.RequestCreatedEvent;
import com.noraemoa.songrec.service.fileStorage.FileStorageService;
import com.noraemoa.songrec.service.fileStorage.S3FileStorageService.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 추천 요청이 커밋된 뒤 썸네일 이미지를 비동기로 만든다.
 *
 * - AFTER_COMMIT: Request 저장이 롤백되면 실행되지 않는다. 그래서 없는 requestId 로 이미지를 만들거나 업로드하지 않는다.
 * - @Async: 사용자는 이미지 생성(10초 이상)을 기다리지 않고 바로 추천 결과를 받는다.
 * - 실패해도 추천 결과에는 영향이 없다. 로그만 남기고 thumbnailUrl 은 null 로 남는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestThumbnailEventListener {

  private final RequestThumbnailAiService requestThumbnailAiService;
  private final FileStorageService fileStorageService;
  private final RequestWriteService requestWriteService;

  @Async(AsyncConfig.THUMBNAIL_EXECUTOR)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void generateThumbnail(RequestCreatedEvent event) {
    long start = System.nanoTime();
    try {
      byte[] imageBytes = requestThumbnailAiService.generateThumbnail(
          event.prompt(), event.title(), event.keywords());
      StoredFile stored = fileStorageService.storeGeneratedThumbnail(event.requestId(), imageBytes);

      // 다른 스레드라 요청 트랜잭션은 이미 끝났다. updateThumbnail 이 새 트랜잭션을 연다.
      requestWriteService.updateThumbnail(event.requestId(), stored.key(), stored.url());
      log.info("THUMBNAIL_DONE requestId={} ms={}", event.requestId(),
          (System.nanoTime() - start) / 1_000_000);
    } catch (Exception e) {
      log.error("THUMBNAIL_FAILED requestId={} ms={}", event.requestId(),
          (System.nanoTime() - start) / 1_000_000, e);
    }
  }
}
