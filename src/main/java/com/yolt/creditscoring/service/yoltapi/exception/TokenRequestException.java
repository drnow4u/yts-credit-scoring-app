package com.yolt.creditscoring.service.yoltapi.exception;

public class TokenRequestException extends RuntimeException {
    public TokenRequestException(String message) {
        super(message);
    }
}
