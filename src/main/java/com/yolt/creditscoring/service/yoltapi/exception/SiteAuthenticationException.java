package com.yolt.creditscoring.service.yoltapi.exception;

/**
 * Exception user will decline consent on bank page.
 */
public class SiteAuthenticationException extends RuntimeException {

    public SiteAuthenticationException(String message) {
        super(message);
    }
}
