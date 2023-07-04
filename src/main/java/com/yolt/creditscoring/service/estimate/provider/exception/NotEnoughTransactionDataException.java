package com.yolt.creditscoring.service.estimate.provider.exception;

public class NotEnoughTransactionDataException extends RuntimeException {
    public NotEnoughTransactionDataException(String message) {
        super(message);
    }
}
