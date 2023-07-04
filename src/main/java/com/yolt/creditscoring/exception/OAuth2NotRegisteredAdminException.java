package com.yolt.creditscoring.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

public class OAuth2NotRegisteredAdminException extends AuthenticationException {

    @Getter
    final String idpId;

    @Getter
    final String provider;

    public OAuth2NotRegisteredAdminException(String idpId, String provider) {
        super("Not registered admin tried to access application");
        this.idpId = idpId;
        this.provider = provider;
    }
}
