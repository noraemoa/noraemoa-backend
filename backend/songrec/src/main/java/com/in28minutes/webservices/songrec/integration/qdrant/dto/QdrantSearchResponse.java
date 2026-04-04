package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QdrantSearchResponse {
  private QdrantResultResponse result;
  private String status;

  @Getter
  @Setter
  public static class QdrantResultResponse {
    private List<Point> points;
  }

  @Getter
  @Setter
  public static class Point{
    private Long id;
    private Double score;
    private List<Float> vector;
    private Object payload;
  }
}
