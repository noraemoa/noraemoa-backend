package com.in28minutes.webservices.songrec.dto.response.user;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AggregatedTasteProfile {
  private Map<String,Integer> tagScores;

  private List<String> moodTags;
  private List<String> sceneTags;
  private List<String> textureTags;
  private List<String> genreTags;
  private List<String> dislikedTags;

  private String baseSummary;
}
