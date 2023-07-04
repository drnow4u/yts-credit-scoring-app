package com.yolt.creditscoring.controller.exception;

import lombok.Value;

import java.util.UUID;

@Value
public class ErrorResponseDTO {
    UUID errorCode;
    String errorType;

    public ErrorResponseDTO() {
        this.errorCode = UUID.randomUUID();
        this.errorType = ErrorType.UNKNOWN.name();
    }
    public ErrorResponseDTO(ErrorType errorType) {
        this.errorCode = UUID.randomUUID();
        this.errorType = errorType.name();
    }
}
