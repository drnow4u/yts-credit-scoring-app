package com.yolt.creditscoring.exception;

import java.util.UUID;

public class ClientEmailConfigurationNotFoundException extends RuntimeException {
    public ClientEmailConfigurationNotFoundException(UUID clientEmailId) {
        super("Client email configuration " + clientEmailId + " was not found");
    }

    public ClientEmailConfigurationNotFoundException(String message) {
        super(message);
    }
}
