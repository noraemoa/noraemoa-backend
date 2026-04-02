package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QdrantSearchResponse {
  private QdrantRetrieveResponse result;
  private String status;

  @Getter
  @Setter
  public static class QdrantRetrieveResponse {
    private List<Point> points;
  }

  @Getter
  @Setter
  public static class Point{
    private Long id;
    private Double score;
    private List<Float> vector;
    private SongPayload payload;
  }
}
