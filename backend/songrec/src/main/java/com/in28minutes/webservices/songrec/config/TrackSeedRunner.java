package com.in28minutes.webservices.songrec.config;

import com.in28minutes.webservices.songrec.service.seed.TrackSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackSeedRunner implements CommandLineRunner {
  private final TrackSeedService trackSeedService;

  @Override
  public void run(String... args) throws Exception {
    trackSeedService.seedInitialCatalog();
  }

}
