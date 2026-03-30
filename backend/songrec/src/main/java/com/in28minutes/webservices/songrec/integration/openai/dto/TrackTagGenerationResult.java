package com.in28minutes.webservices.songrec.integration.openai.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackTagGenerationResult {
  private List<String> mood_tags;
  private List<String> scene_tags;
  private List<String> texture_tags;
  private String short_description;
  private String confidence_note;
}
