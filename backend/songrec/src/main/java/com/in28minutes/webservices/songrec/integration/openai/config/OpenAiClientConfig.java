package com.in28minutes.webservices.songrec.integration.openai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAiClientConfig {

  @Bean
  public WebClient openAiWebClient(OpenAiProperties properties) {

    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(configurer ->
            configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)
        )
        .build();

    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("Authorization", "Bearer " + properties.getApiKey())
        .defaultHeader("Content-Type", "application/json")
        .exchangeStrategies(strategies)
        .build();
  }
}