package com.noraemoa.songrec.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @Async 설정. 썸네일 생성 전용 스레드 풀을 따로 둔다.
 *
 * 기본 executor 를 쓰지 않는 이유:
 * - 스프링 부트 기본 executor(applicationTaskExecutor)는 큐 크기가 무제한(Integer.MAX_VALUE)이다.
 *   OpenAI 이미지 API 가 느려지면 작업이 메모리에 끝없이 쌓인다.
 * - 다른 @Async 작업과 스레드를 같이 쓰게 되어, 오래 걸리는 이미지 생성이 다른 작업을 막을 수 있다.
 * - 스레드 이름이 thumbnail-N 으로 찍혀서 로그에서 어떤 작업인지 바로 알 수 있다.
 */
@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

  public static final String THUMBNAIL_EXECUTOR = "thumbnailExecutor";

  @Bean(name = THUMBNAIL_EXECUTOR)
  public ThreadPoolTaskExecutor thumbnailExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    // 이미지 생성은 건당 10초 이상 걸리고 OpenAI 요청 한도도 있어서 동시 실행 수를 작게 잡는다.
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    // 큐가 다 차야 max 까지 스레드가 늘어난다. 50건을 넘으면 거절한다.
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("thumbnail-");
    // 큐가 꽉 차면 요청 스레드에서 실행하지 않고(CallerRuns 는 응답을 다시 느리게 만든다) 경고만 남기고 버린다.
    // 썸네일이 없어도 추천 결과는 정상이고, 사용자가 직접 업로드할 수도 있다.
    executor.setRejectedExecutionHandler((task, pool) ->
        log.warn("Thumbnail task rejected: queue is full (active={}, queued={})",
            pool.getActiveCount(), pool.getQueue().size()));
    // 앱 종료 시 진행 중인 썸네일 작업이 끝날 때까지 최대 30초 기다린다.
    executor.setWaitForTasksToCompleteOnShutdown(true);
    executor.setAwaitTerminationSeconds(30);
    executor.initialize();
    return executor;
  }

  // void 반환 @Async 메서드에서 잡히지 않은 예외는 호출자에게 전달되지 않으므로 여기서 로그로 남긴다.
  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return (ex, method, params) ->
        log.error("Uncaught async exception in {}", method.getName(), ex);
  }
}
