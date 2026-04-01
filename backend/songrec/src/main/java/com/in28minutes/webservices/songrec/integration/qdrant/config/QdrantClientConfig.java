package com.in28minutes.webservices.songrec.integration.qdrant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class QdrantClientConfig {
  @Bean
  public WebClient qdrantWebClient(QdrantProperties properties){
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(configurer ->
            configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) // 10MB
        )
        .build();
    return WebClient.builder()
        .baseUrl(properties.getUrl())
        .exchangeStrategies(strategies)
        .build();
  }
}
