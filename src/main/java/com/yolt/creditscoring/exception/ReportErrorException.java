package com.yolt.creditscoring.exception;

public class ReportErrorException extends RuntimeException { // 500

    public ReportErrorException(String message) {
        super(message);
    }
}
