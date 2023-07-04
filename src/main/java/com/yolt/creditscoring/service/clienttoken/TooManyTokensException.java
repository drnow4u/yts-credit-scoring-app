package com.yolt.creditscoring.service.clienttoken;

public class TooManyTokensException extends RuntimeException {
    public TooManyTokensException(String message) {
        super(message);
    }
}
