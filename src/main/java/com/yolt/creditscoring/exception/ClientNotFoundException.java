package com.yolt.creditscoring.exception;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException(UUID clientId) {
        super("Client " + clientId + " not found.");
    }
}
