package com.yolt.creditscoring.exception;

public class JwtCreationException extends RuntimeException {

    public JwtCreationException(String message) {
        super(message);
    }
}
