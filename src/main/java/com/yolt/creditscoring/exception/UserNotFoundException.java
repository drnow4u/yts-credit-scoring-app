package com.yolt.creditscoring.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
    public UserNotFoundException(UUID userId) {
        super("User was not found with ID: " + userId);
    }
}
