package com.in28minutes.webservices.songrec.dto.response;

import com.in28minutes.webservices.songrec.domain.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponseDto {
    private Long id;
    private String name;
    private String role;

    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
