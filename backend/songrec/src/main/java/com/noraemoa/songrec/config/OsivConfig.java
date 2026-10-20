package com.noraemoa.songrec.config;

import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * spring.jpa.open-in-view=false 로 전역 OSIV를 끈 뒤,
 * 요청 생성 API(POST /requests)만 제외하고 나머지 API에는 OSIV를 다시 적용한다.
 *
 * OSIV가 켜져 있으면 요청 처리 중 처음 DB에 접근한 순간 잡은 커넥션을 응답이 끝날 때까지 유지한다.
 * 요청 생성 API는 OpenAI/Qdrant/S3 같은 외부 호출이 수 초 걸리므로, 이 동안 커넥션을 붙잡지 않도록 제외했다.
 * (나머지 API는 컨트롤러/DTO에서 LAZY 필드를 읽는 곳이 있어 기존 동작을 유지)
 */
@Configuration
@RequiredArgsConstructor
public class OsivConfig implements WebMvcConfigurer {

  private final EntityManagerFactory entityManagerFactory;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    OpenEntityManagerInViewInterceptor osiv = new OpenEntityManagerInViewInterceptor();
    osiv.setEntityManagerFactory(entityManagerFactory);
    registry.addWebRequestInterceptor(osiv)
        .addPathPatterns("/**")
        .excludePathPatterns("/requests", "/requests/");
  }
}