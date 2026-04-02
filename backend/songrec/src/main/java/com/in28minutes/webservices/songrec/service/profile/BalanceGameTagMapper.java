package com.in28minutes.webservices.songrec.service.profile;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BalanceGameTagMapper {

  private final Map<String, List<String>> choiceTageMap = Map.ofEntries(
      Map.entry("space_dark_room", List.of("calm", "cozy", "introspective", "lofi", "night")),
      Map.entry("space_bright_open", List.of("bright", "open", "uplifting", "airy", "daytime")),
      Map.entry("discovery_keep_myself",
          List.of("personal", "indie", "niche", "solo", "introspective")),
      Map.entry("discovery_share_now",
          List.of("social", "popular", "mainstream", "upbeat", "energetic")),
      Map.entry("lost_keep_walking",
          List.of("wandering", "exploratory", "ambient", "chill", "indie")),
      Map.entry("lost_open_map", List.of("focused", "structured", "rhythmic", "clean", "pop")),
      Map.entry("important_lyrics",
          List.of("lyrical", "vocal", "emotional", "storytelling", "ballad")),
      Map.entry("important_vibe",
          List.of("atmospheric", "vibe", "instrumental", "texture_driven", "dreamy")),
      Map.entry("scene_window_thinking",
          List.of("late-night", "solo", "reflective", "melancholic", "calm")),
      Map.entry("scene_drive_loud", List.of("drive", "roadtrip", "energetic", "free", "upbeat")),
      Map.entry("weather_rainy", List.of("rainy", "melancholic", "soft", "emotional", "moody")),
      Map.entry("weather_sunny", List.of("sunny", "bright", "happy", "light", "uplifting")),
      Map.entry("moment_2am", List.of("late-night", "deep", "introspective", "slow", "emotional")),
      Map.entry("moment_3pm", List.of("daytime", "active", "playful", "light", "upbeat")),
      Map.entry("walking_slow", List.of("walking", "slow", "reflective", "calm", "ambient")),
      Map.entry("walking_musicvideo",
          List.of("rhythmic", "energetic", "confident", "danceable", "pop"))
  );

  public List<String> getTags(String choiceKey) {
    return choiceTageMap.getOrDefault(choiceKey, List.of());
  }
}
