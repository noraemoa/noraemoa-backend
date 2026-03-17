package com.in28minutes.webservices.songrec.integration.openai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OpenAiClientConfig {

  @Bean
  public WebClient openAiWebClient(OpenAiProperties properties) {
    System.out.println("openai api key null? " + (properties.getApiKey() == null));
    System.out.println("openai api key length = " +
        (properties.getApiKey() == null ? 0 : properties.getApiKey().length()));

    if (properties.getApiKey() != null && properties.getApiKey().length() >= 12) {
      System.out.println("openai api key prefix = " + properties.getApiKey().substring(0, 7));
      System.out.println("openai api key suffix = " +
          properties.getApiKey().substring(properties.getApiKey().length() - 4));
    }
    return WebClient.builder()
        .baseUrl(properties.getBaseUrl())
        .defaultHeader("Authorization","Bearer"+properties.getApiKey())
        .defaultHeader("Content-Type","application/json")
        .build();
  }
}
