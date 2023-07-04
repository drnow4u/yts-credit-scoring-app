package com.yolt.creditscoring.service.yoltapi.exception;

/**
 * Exception used when some technical error will occur during bank consent.
 */
public class SiteCreationException extends RuntimeException {

    public SiteCreationException(String message) {
        super(message);
    }
}
