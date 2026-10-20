package com.noraemoa.songrec.integration.qdrant.dto;

import com.noraemoa.songrec.domain.track.Track;
import com.noraemoa.songrec.integration.qdrant.dto.QdrantSearchResponse.Point;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RerankedCandidate {
  private Point point;
  private Track track;
  private double finalScore;
}
