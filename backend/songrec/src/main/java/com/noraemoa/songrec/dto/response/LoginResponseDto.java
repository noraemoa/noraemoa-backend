package com.noraemoa.songrec.dto.response;

import lombok.*;

@Getter @Builder
public class LoginResponseDto {
    private Long userId;
    private String email;
    private String username;
    private String role;
    private String accessToken;
}
