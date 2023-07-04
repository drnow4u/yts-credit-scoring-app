package com.yolt.creditscoring.service.securitymodule.signature;

import java.security.GeneralSecurityException;

public class SignatureDeserializationException extends RuntimeException {
    public SignatureDeserializationException(String message, GeneralSecurityException e) {
        super(message, e);
    }
}
