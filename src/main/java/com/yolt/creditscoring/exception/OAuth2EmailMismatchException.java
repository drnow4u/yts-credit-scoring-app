package com.yolt.creditscoring.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

import java.util.UUID;

@Getter
public class OAuth2EmailMismatchException extends AuthenticationException {

    private final UUID clientID;
    private final String idpId;
    private final String storedClientAdminEmail;
    private final String providedClientAdminEmail;
    private final String provider;

    @Builder
    public OAuth2EmailMismatchException(UUID clientID, String idpId, String storedClientAdminEmail, String providedClientAdminEmail, String provider) {
        super("Email mismatch between stored client admin email and provided one");
        this.clientID = clientID;
        this.idpId = idpId;
        this.storedClientAdminEmail = storedClientAdminEmail;
        this.providedClientAdminEmail = providedClientAdminEmail;
        this.provider = provider;
    }
}
