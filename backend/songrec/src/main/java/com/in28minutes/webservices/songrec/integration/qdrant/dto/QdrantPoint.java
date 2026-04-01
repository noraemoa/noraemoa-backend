package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QdrantPoint {
  private Object id; // track_id 저장 (spotify_id 아님)
  private List<Float> vector;
  private Object payload;
}
