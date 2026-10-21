package com.noraemoa.songrec.config.perf;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * POST /requests 1건의 전체 응답 시간과 커넥션 사용량을 한 줄로 남긴다.
 * 커넥션 수치는 perf.connection-tracking.enabled=true 일 때만 채워진다(아니면 0).
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCreatePerfFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return !("POST".equals(request.getMethod())
        && ("/requests".equals(uri) || "/requests/".equals(uri)));
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    long start = System.nanoTime();
    ConnectionUsageTracker.start();
    try {
      filterChain.doFilter(request, response);
    } finally {
      long totalMs = (System.nanoTime() - start) / 1_000_000;
      ConnectionUsageTracker.Stats stats = ConnectionUsageTracker.stop();
      log.info(
          "PERF_REQUEST_CREATE totalMs={} connCount={} connHoldTotalMs={} connHoldMaxMs={} connWaitTotalMs={} status={}",
          totalMs, stats.count(), stats.holdTotalMs(), stats.holdMaxMs(), stats.waitTotalMs(),
          response.getStatus());
    }
  }
}
