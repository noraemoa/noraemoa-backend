package com.in28minutes.webservices.songrec.config;

import com.in28minutes.webservices.songrec.integration.qdrant.dto.SongPayload;
import com.in28minutes.webservices.songrec.service.qdrant.SongVectorIndexService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class QdrantSongTestRunner implements CommandLineRunner {

  private final SongVectorIndexService songVectorIndexService;

  @Override
  public void run(String... args) {
    SongPayload payload = SongPayload.builder()
        .trackId(1L)
        .spotifyTrackId("4Dr2hJ3EnVh2Aaot6fRwDO")
        .title("Blueming")
        .artist("IU")
        .mood_tags(List.of("bright",
            "romantic",
            "cheerful",
            "light"))
        .scene_tags(List.of("daytime walk",
            "spring day",
            "casual listening",
            "happy mood"))
        .texture_tags(List.of("clean vocal",
            "light pop",
            "melodic",
            "smooth"))
        .build();

    songVectorIndexService.upsertSong(payload);
    System.out.println("Qdrant sample song upsert completed");
  }

}
