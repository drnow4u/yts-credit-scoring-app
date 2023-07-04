package com.yolt.creditscoring.service.client;

import java.util.UUID;

public class ClientFeatureDisabledException extends RuntimeException {
    public ClientFeatureDisabledException(String message) {
        super(message);
    }
}
