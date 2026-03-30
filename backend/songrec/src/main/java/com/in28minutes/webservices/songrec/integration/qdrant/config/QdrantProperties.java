package com.in28minutes.webservices.songrec.integration.qdrant.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "qdrant")
public class QdrantProperties {
  private String url;
  private String collectionName;
  private Integer vectorSize;
  private String distance;
}
