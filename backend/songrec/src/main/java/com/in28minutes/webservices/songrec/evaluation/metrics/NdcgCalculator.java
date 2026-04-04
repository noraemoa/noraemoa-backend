package com.in28minutes.webservices.songrec.evaluation.metrics;

import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NdcgCalculator {

  private final TagRelevanceCalculator tagRelevanceCalculator;

  public double calculateNDCG(List<Point> responseSongs, Set<String> queryTags) {
    if (responseSongs == null || responseSongs.isEmpty()) {
      return 0.0;
    }

    int limit = Math.min(10, responseSongs.size());
    List<Integer> relevances = new ArrayList<>();
    for (int i = 1; i < limit + 1; i++) {
      Point response = responseSongs.get(i - 1);
      double matchRatio = tagRelevanceCalculator.calculateTagRelevance(queryTags, response);
      int relevance = toGradedRelevance(matchRatio);
      relevances.add(relevance);
    }

    double dcg = calculateDCG(relevances);

    List<Integer> idealRelevances = new ArrayList<>(relevances);
    idealRelevances.sort(Collections.reverseOrder());
    double idcg = calculateDCG(idealRelevances);

    if(idcg==0.0) return 0.0;

    return dcg / idcg;
  }

  public double calculateDCG(List<Integer> relevances) {
    double dcg = 0.0;
    for (int i = 1; i < relevances.size()+1 ; i++) {
      int relevance = relevances.get(i-1);
      double denominator = Math.log(i + 1) / Math.log(2);
      dcg += relevance / denominator;
    }
    return dcg;
  }


  public int toGradedRelevance(double matchRatio) {
    if (matchRatio >= 0.75) {
      return 3;
    }
    if (matchRatio >= 0.5) {
      return 2;
    }
    if (matchRatio >= 0.25) {
      return 1;
    }
    return 0;
  }

}
