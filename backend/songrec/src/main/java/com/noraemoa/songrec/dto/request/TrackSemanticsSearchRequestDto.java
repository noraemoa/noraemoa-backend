package com.noraemoa.songrec.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackSemanticsSearchRequestDto {
  @NotBlank
  private String query;

  private Integer limit=10;
}
