package com.in28minutes.webservices.songrec.integration.spotify.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifySearchResponse(
        Tracks tracks
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tracks(
            Integer total,
            List<TrackItem> items
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TrackItem(
            String id,
            String name,
            @JsonProperty("duration_ms") int durationMs,
            Album album,
            List<Artist> artists
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Album(
            String id,
            String name,
            List<Image> images
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(
            String url,
            Integer height,
            Integer width
    ){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Artist(
            String id,
            String name
    ){}

}
