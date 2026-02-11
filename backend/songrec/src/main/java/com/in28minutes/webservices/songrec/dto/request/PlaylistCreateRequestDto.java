package com.in28minutes.webservices.songrec.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistCreateRequestDto {
    @NotBlank(message = "플레이리스트 제목을 입력해주세요.")
    @Size(max = 500, message = "요청은 500자 이하여야 합니다.")
    private String title;
}
