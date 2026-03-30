package com.in28minutes.webservices.songrec.integration.spotify.api;

import com.in28minutes.webservices.songrec.integration.spotify.auth.SpotifyTokenManager;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifyGetArtistResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// api
@Service
@RequiredArgsConstructor
public class SpotifyApiClient {
    private final SpotifyTokenManager tokenManager;
    private final WebClient webClient = WebClient.create("https://api.spotify.com");

    public SpotifySearchResponse search(String query) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search")
                        .queryParam("q",query)
                        .queryParam("type","track")
                        .queryParam("market","KR")
                        .queryParam("limit",10)
                        .build())
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+tokenManager.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifySearchResponse.class)
                .block();
    }

    public SpotifyGetArtistResponse getArtist(String id) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/artists/{id}")
                        .build(id))
                .header(HttpHeaders.AUTHORIZATION,"Bearer "+tokenManager.getAccessToken())
                .retrieve()
                .bodyToMono(SpotifyGetArtistResponse.class)
                .block();

    }
}
