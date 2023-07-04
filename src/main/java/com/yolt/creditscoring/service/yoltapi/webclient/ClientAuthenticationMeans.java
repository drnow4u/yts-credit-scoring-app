package com.yolt.creditscoring.service.yoltapi.webclient;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.security.PrivateKey;
import java.util.UUID;

@Value
@Builder
public class ClientAuthenticationMeans {

    @NonNull
    UUID clientId;

    @NonNull
    UUID requestTokenPublicKeyId;

    @NonNull
    PrivateKey signingPrivateKey;

    @NonNull
    String accessToken;

    @NonNull
    UUID redirectUrlId;
}
