package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class QdrantUpsertPointsRequest {
  private List<QdrantPoint> points;
}
