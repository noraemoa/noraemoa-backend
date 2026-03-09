package com.in28minutes.webservices.songrec.integration.spotify.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SpotifyArtistResponseDto {
    private List<Image> images;
    private String name;

    @Getter @Builder
    public static class Image{
        private String url;
        private Integer height;
        private Integer width;
    }

    public static SpotifyArtistResponseDto from(SpotifyGetArtistResponse res){
        var images=res.images().stream().map(i->
                Image.builder()
                        .url(i.url())
                        .height(i.height())
                        .width(i.width())
                        .build()).toList();
        return SpotifyArtistResponseDto.builder()
                .images(images)
                .name(res.name()).build();
    }
}
