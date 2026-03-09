package com.in28minutes.webservices.songrec.integration.spotify.auth;

import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.util.Base64;

// 토큰 요청
@Component
@RequiredArgsConstructor
public class SpotifyAuthClient {
    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final WebClient webClient=WebClient.create();

    public SpotifyTokenResponse requestToken(){
        String credentials = Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes());

            try {
                return webClient.post()
                        .uri("https://accounts.spotify.com/api/token")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
                        .retrieve()
                        .bodyToMono(SpotifyTokenResponse.class)
                        .block();
            } catch (WebClientRequestException e) {
                throw new RuntimeException("Spotify 서버에 연결할 수 없습니다.",e);
            }

    }

}
