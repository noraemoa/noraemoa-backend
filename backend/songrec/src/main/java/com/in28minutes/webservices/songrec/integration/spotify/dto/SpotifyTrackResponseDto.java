package com.in28minutes.webservices.songrec.integration.spotify.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SpotifyTrackResponseDto {
    private Integer total;
    private List<Item> items;

    @Getter
    @Builder
    public static class Item {
        private String trackId;
        private String name;
        private Integer durationMs;
        private List<Artist> artists;
        private Album album;
    }

    @Getter @Builder
    public static class Artist {
        private String artistId;
        private String name;
    }

    @Getter @Builder
    public static class Album {
        private String albumId;
        private List<Image> albumImages;
    }

    @Getter @Builder
    public static class Image {
        private String url;
        private Integer height;
        private Integer width;
    }

    public static SpotifyTrackResponseDto from(SpotifySearchResponse res){
        var items = res.tracks().items().stream().map(t->
                Item.builder()
                        .trackId(t.id())
                        .name(t.name())
                        .durationMs(t.durationMs())
                        .artists(t.artists().stream().map(a->
                                Artist.builder().artistId(a.id()).name(a.name()).build()).toList())
                        .album(Album.builder()
                                .albumId(t.album().id())
                                .albumImages(t.album().images().stream().map(i->
                                    Image.builder()
                                            .url(i.url()).height(i.height()).width(i.width()).build()).toList()).build())
                        .build()).toList();

        return SpotifyTrackResponseDto.builder().total(res.tracks().total()).items(items).build();
    }
}
