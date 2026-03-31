package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QdrantSearchResponse {
  private Result result;
  private String status;

  @Getter
  @Setter
  public static class Result{
    private List<Point> points;
  }

  @Getter
  @Setter
  public static class Point{
    private Object id; //trackId
    private Double score;
    private SongPayload payload;
  }
}
