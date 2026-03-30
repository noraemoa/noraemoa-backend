package com.in28minutes.webservices.songrec.integration.openai.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmbeddingResponse {
  private List<EmbeddingData> data; //data[0].embedding 에 float 배열 있음.
  private String model;
  private Usage usage;

  @Getter
  @Setter
  public static class EmbeddingData{
    private Integer index;
    private List<Float> embedding;
  }

  @Getter
  @Setter
  public static class Usage{
    private Integer prompt_tokens;
    private Integer total_tokens;
  }
}
