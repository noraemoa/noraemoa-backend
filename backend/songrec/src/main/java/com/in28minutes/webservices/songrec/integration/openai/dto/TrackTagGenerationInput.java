package com.in28minutes.webservices.songrec.integration.openai.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TrackTagGenerationInput {
  private String spotifyId;
  private String name;
  private String artist;
  private String albumName;
  private Integer durationMs;
}
