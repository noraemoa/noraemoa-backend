package com.in28minutes.webservices.songrec.evaluation.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EvaluateResultDto {
  private double beforePrecision;
  private double afterPrecision;
  private double beforeNDCG;
  private double afterNDCG;
  private double beforeLikedSimilarity;
  private double afterLikedSimilarity;
}
