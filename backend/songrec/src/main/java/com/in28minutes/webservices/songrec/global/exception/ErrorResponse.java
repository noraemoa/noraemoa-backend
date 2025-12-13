package com.in28minutes.webservices.songrec.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class ErrorResponse {
    LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    private List<FieldErrorResponse> fields;
}
