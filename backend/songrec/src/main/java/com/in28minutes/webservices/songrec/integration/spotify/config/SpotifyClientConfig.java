package com.in28minutes.webservices.songrec.integration.spotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SpotifyClientConfig {
    @Bean
    public WebClient spotifyWebClient(){
        return WebClient.builder()
                .baseUrl("https://api.spotify.com")
                .build();
    }

    @Bean
    public WebClient spotifyAuthWebClient(){
        return WebClient.builder()
                .baseUrl("https://accounts.spotify.com")
                .build();
    }

}
