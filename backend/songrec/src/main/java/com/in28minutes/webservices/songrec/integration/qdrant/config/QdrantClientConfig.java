package com.in28minutes.webservices.songrec.integration.qdrant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantClientConfig {
  @Bean
  public WebClient qdrantWebClient(QdrantProperties properties){
    return WebClient.builder()
        .baseUrl(properties.getUrl())
        .build();
  }
}
