package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import com.in28minutes.webservices.songrec.domain.track.Track;
import com.in28minutes.webservices.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RerankedCandidate {
  private Point point;
  private Track track;
  private double finalScore;
}
