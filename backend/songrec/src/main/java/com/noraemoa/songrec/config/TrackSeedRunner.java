package com.noraemoa.songrec.config;

import com.noraemoa.songrec.service.seed.TrackSeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class TrackSeedRunner implements CommandLineRunner {
  private final TrackSeedService trackSeedService;

  @Override
  public void run(String... args) throws Exception {
    trackSeedService.seedInitialCatalog();
  }

}
