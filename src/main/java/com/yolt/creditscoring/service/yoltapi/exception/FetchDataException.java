package com.yolt.creditscoring.service.yoltapi.exception;

public class FetchDataException extends RuntimeException {
    public FetchDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public FetchDataException(String message) {
        super(message);
    }
}
