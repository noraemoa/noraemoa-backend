package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SongPayload {
  private Long trackId;
  private String spotifyTrackId;
  private String title;
  private String artist; //artist 이름

  private List<String> mood_tags;
  private List<String> scene_tags;
  private List<String> texture_tags;
  private String short_description;
}
