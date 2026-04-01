package com.in28minutes.webservices.songrec.integration.openai.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserTasteProfileResult {
  private List<String> preferred_mood_tags;
  private List<String> preferred_scene_tags;
  private List<String> preferred_texture_tags;
  private List<String> preferred_genre_tags;
  private List<String> disliked_tags;
  private String profile_summary;
}
