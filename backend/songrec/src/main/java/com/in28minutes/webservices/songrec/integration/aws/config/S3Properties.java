package com.in28minutes.webservices.songrec.integration.aws.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {
  private String bucket;
  private String region;
  private String baseUrl;
}
