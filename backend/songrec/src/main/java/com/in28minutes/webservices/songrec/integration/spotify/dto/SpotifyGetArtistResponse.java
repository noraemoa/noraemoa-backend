package com.in28minutes.webservices.songrec.integration.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyGetArtistResponse(
        List<Image> images,
        String name
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(
            String url,
            Integer height,
            Integer width
    ){}

}