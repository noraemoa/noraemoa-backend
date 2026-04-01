package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfilePayload {
  private Long userId;
  private List<String> preferred_mood_tags;
  private List<String> preferred_scene_tags;
  private List<String> preferred_texture_tags;
  private List<String> preferred_genre_tags;
  private List<String> disliked_tags;
  private String profile_summary;
}
