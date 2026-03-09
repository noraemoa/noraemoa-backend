package com.in28minutes.webservices.songrec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrackCreateRequestDto {
    @NotBlank
    private String spotifyId;

    @NotBlank
    private String name;

    private String artist;
    private String album;

    private String imageUrl;

    private Integer durationMs;
}
