package com.in28minutes.webservices.songrec.service.profile;

import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto;
import com.in28minutes.webservices.songrec.dto.request.UserTasteProfileCreateRequestDto.BalanceAnswerDto;
import com.in28minutes.webservices.songrec.dto.response.user.AggregatedTasteProfile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceGameTagAggregator {
  private final BalanceGameTagMapper balanceGameTagMapper;
  private static final Set<String> MOOD_TAGS = Set.of(
      "calm", "uplifting", "melancholic", "emotional", "happy",
      "moody", "energetic", "reflective", "playful", "deep", "light"
  );

  private static final Set<String> SCENE_TAGS = Set.of(
      "night", "late-night", "daytime", "rainy", "sunny",
      "drive", "roadtrip", "walking", "solo"
  );

  private static final Set<String> TEXTURE_TAGS = Set.of(
      "lofi", "airy", "ambient", "soft", "dreamy",
      "instrumental", "vocal", "rhythmic", "danceable", "cozy", "texture_driven"
  );

  private static final Set<String> GENRE_TAGS = Set.of(
      "indie", "pop", "ballad"
  );

  public AggregatedTasteProfile aggregate(UserTasteProfileCreateRequestDto dto){
    Map<String,Integer> tagScore = new HashMap<>();
    for(BalanceAnswerDto choice:dto.getAnswers()){
      List<String> tags = balanceGameTagMapper.getTags(choice.getChoiceKey());
      for(String tag:tags){
        tagScore.merge(tag,1,Integer::sum);
      }
    }
    List<String> moodTags = topTags(tagScore,MOOD_TAGS,5);
    List<String> sceneTags = topTags(tagScore, SCENE_TAGS, 5);
    List<String> textureTags = topTags(tagScore, TEXTURE_TAGS, 5);
    List<String> genreTags = resolveGenreTags(tagScore);
    List<String> dislikedTags = resolveDislikedTags(tagScore);

    String baseSummary = buildBaseSummary(moodTags, sceneTags, textureTags, genreTags);

    return AggregatedTasteProfile.builder()
        .tagScores(tagScore)
        .moodTags(moodTags)
        .sceneTags(sceneTags)
        .textureTags(textureTags)
        .genreTags(genreTags)
        .dislikedTags(dislikedTags)
        .baseSummary(baseSummary)
        .build();
  }

  private List<String> resolveGenreTags(Map<String, Integer> scores) {
    List<String> result = new ArrayList<>(topTags(scores, GENRE_TAGS, 3));

    if (score(scores, "lyrical") + score(scores, "emotional") + score(scores, "vocal") >= 4
        && !result.contains("ballad")) {
      result.add("ballad");
    }

    if (score(scores, "indie") + score(scores, "ambient") + score(scores, "reflective") >= 4
        && !result.contains("indie")) {
      result.add("indie");
    }

    if (score(scores, "rhythmic") + score(scores, "upbeat") + score(scores, "danceable") >= 4
        && !result.contains("pop")) {
      result.add("pop");
    }

    return result.stream().distinct().limit(3).toList();
  }
  private int score(Map<String, Integer> scores, String tag) {
    return scores.getOrDefault(tag, 0);
  }

  private List<String> resolveDislikedTags(Map<String, Integer> scores) {
    List<String> result = new ArrayList<>();

    if (score(scores, "calm") + score(scores, "reflective") + score(scores, "slow") >= 3) {
      result.add("aggressive");
      result.add("noisy");
    }

    if (score(scores, "atmospheric") + score(scores, "ambient") + score(scores, "dreamy") >= 3) {
      result.add("vocal-heavy");
    }

    if (score(scores, "lyrical") + score(scores, "ballad") >= 2) {
      result.add("beat-heavy");
    }

    if (score(scores, "energetic") + score(scores, "danceable") + score(scores, "drive") >= 3) {
      result.add("sleepy");
      result.add("muted");
    }

    return result.stream().distinct().limit(5).toList();
  }

  private String buildBaseSummary(
      List<String> moodTags,
      List<String> sceneTags,
      List<String> textureTags,
      List<String> genreTags
  ) {
    return "Prefers " + String.join(", ", moodTags)
        + " moods, especially in " + String.join(", ", sceneTags)
        + " settings, with " + String.join(", ", textureTags)
        + " textures and broad affinity for " + String.join(", ", genreTags) + ".";
  }

  private List<String> topTags(Map<String, Integer> tagScore,Set<String> category,int limit){
    return tagScore.entrySet().stream()
        .filter(e->category.contains(e.getKey()))
        .sorted((a,b)->{
          int cmp = Integer.compare(b.getValue(),a.getValue());
          if(cmp!=0) return cmp;
          return a.getKey().compareTo(b.getKey());
        })
        .map(Map.Entry::getKey)
        .limit(limit)
        .toList();
  }
}
