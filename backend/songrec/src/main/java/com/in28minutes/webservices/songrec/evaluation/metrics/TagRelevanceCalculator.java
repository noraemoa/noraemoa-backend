package com.in28minutes.webservices.songrec.evaluation.metrics;

import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.SongPayload;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class TagRelevanceCalculator {

  public double calculatePrecision(
      Set<String> queryTags,
      List<Point> responseSongs) {
    if (responseSongs == null || responseSongs.isEmpty()) {
      return 0.0;
    }

    int limit = Math.min(10, responseSongs.size());
    int relevantCnt = 0;

    for (Point response : responseSongs) {
      double matchRatio = calculateTagRelevance(queryTags, response);
      if (matchRatio >= 0.5) {
        relevantCnt++;
      }
    }
    return (double) relevantCnt / limit;
  }

  public double calculateTagRelevance(
      Set<String> queryTags,
      Point responseSong
  ) {
    if (queryTags.isEmpty()) {
      return 0.0;
    }
    Set<String> songTags = new HashSet<>();
    SongPayload songPayload = (SongPayload )responseSong.getPayload();
    songTags.addAll(normalize(songPayload.getMood_tags()));
    songTags.addAll(normalize(songPayload.getScene_tags()));
    songTags.addAll(normalize(songPayload.getTexture_tags()));

    int totalTagCount = queryTags.size();
    int matchCount = 0;
    for (String tag : songTags) {
      if (queryTags.contains(tag)) {
        matchCount++;
      }
    }
    return (double) matchCount / totalTagCount;
  }

  public Set<String> normalize(List<String> tags) {
    Set<String> result = new HashSet<>();
    if (tags == null) {
      return result;
    }

    for (String tag : tags) {
      if (tag == null) {
        continue;
      }
      result.add(tag.trim().toLowerCase());
    }
    return result;
  }
}
