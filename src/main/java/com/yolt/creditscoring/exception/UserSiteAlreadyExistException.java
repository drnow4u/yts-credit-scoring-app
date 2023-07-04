package com.yolt.creditscoring.exception;

public class UserSiteAlreadyExistException extends RuntimeException {
    public UserSiteAlreadyExistException(String message) {
        super(message);
    }
}
