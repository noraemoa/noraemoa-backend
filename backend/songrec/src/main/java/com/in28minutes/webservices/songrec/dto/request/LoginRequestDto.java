package com.in28minutes.webservices.songrec.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Builder
public class LoginRequestDto {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
}
