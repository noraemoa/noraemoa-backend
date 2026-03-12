package com.in28minutes.webservices.songrec.dto.response.user;

import com.in28minutes.webservices.songrec.domain.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class UserResponseDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
