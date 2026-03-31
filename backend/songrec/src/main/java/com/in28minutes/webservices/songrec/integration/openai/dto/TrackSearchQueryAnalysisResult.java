package com.in28minutes.webservices.songrec.integration.openai.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TrackSearchQueryAnalysisResult {
  private List<String> mood_tags;
  private List<String> scene_tags;
  private List<String> texture_tags;
  private List<String> genre_tags;
  private List<String> exclude_tags;
  private String language;
  private String short_description;
}
