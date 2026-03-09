package com.in28minutes.webservices.songrec.integration.spotify.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

// 토큰 만료 60초 전에 재발급
@Service
@RequiredArgsConstructor
public class SpotifyTokenManager {
    private final SpotifyAuthClient spotifyAuthClient;

    private String accessToken;
    private Instant expiresAt;

    public synchronized String getAccessToken() {
        if(accessToken == null || expiresAt==null ||Instant.now().isAfter(expiresAt)){
            var response = spotifyAuthClient.requestToken();
            accessToken =response.getAccessToken();
            expiresAt=Instant.now().plusSeconds(response.getExpiresIn() - 60);
        }
        return accessToken;
    }
}
