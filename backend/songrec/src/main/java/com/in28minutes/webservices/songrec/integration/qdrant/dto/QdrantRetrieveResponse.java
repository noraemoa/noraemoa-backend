package com.in28minutes.webservices.songrec.integration.qdrant.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QdrantRetrieveResponse {
  private List<QdrantPoint> result;
  private String status;
}
