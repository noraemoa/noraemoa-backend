package com.in28minutes.webservices.songrec.service.seed;

import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.dto.request.TrackCreateRequestDto;
import com.in28minutes.webservices.songrec.integration.spotify.api.SpotifyApiClient;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse;
import com.in28minutes.webservices.songrec.integration.spotify.dto.SpotifySearchResponse.TrackItem;
import com.in28minutes.webservices.songrec.service.TrackService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackSeedService {

  private final SpotifyApiClient spotifyApiClient;
  private final SeedQueryProvider seedQueryProvider;
  private final TrackService trackService;

  public void seedInitialCatalog() {
    List<String> queries = seedQueryProvider.getSeedQueries();

    for (String query : queries) {
      seedByQuery(query);
    }
  }

  public void seedByQuery(String query) {
    log.info("Start seeding query={}", query);

    SpotifySearchResponse response = null;
    try {
      response = spotifyApiClient.search(query);
    } catch (Exception e) {
      log.warn("Spotify search failed.", query, e);
    }

    if (response == null
        || response.tracks() == null
        || response.tracks().items() == null
        || response.tracks().items().isEmpty()) {
      log.info("No more tracks for query={}, offset={}", query);
      return;
    }

    for (TrackItem item : response.tracks().items()) {
      try {
        TrackCreateRequestDto dto = trackService.toTrackCreateRequestDto(item);

        Track track = trackService.findOrCreateTrack(dto);

        // 아직 인덱싱 안 된 곡만 OpenAI + Qdrant 수행
        trackService.ensureTrackIndexed(track, dto);

      } catch (Exception e) {
        log.warn("Failed to seed track. query={}, spotifyId={}",
            query, item.id(), e);
      }
    }

    log.info("Finished seeding query={}", query);
  }
}
