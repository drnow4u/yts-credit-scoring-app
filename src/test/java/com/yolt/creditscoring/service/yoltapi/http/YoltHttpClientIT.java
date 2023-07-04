package com.yolt.creditscoring.service.yoltapi.http;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.yoltapi.configuration.YoltApiProperties;
import com.yolt.creditscoring.service.yoltapi.http.model.AccessTokenResponse;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class YoltHttpClientIT {

    @Autowired
    VaultSecretKeyService secretKeyService;

    @Autowired
    YoltApiProperties yoltApiProperties;

    @Autowired
    YoltHttpClient yoltHttpClient;

    @Test
    void shouldGetAccessToken() throws JoseException, NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        ClientAuthenticationMeans authenticationMean = ClientAuthenticationMeans.builder()
                .clientId(yoltApiProperties.getClientId())
                .requestTokenPublicKeyId(yoltApiProperties.getRequestTokenPublicKeyId())
                .redirectUrlId(yoltApiProperties.getRedirectUrlId())
                .signingPrivateKey(secretKeyService.getSigningPrivateKey())
                .accessToken("Invalid token")
                .build();

        // When
        AccessTokenResponse accessToken = yoltHttpClient.getAccessToken(authenticationMean);

        // Then
        AccessTokenResponse expectedToken = new AccessTokenResponse();
        expectedToken.setAccessToken("eyJhbGciOiJkaXIiLCJlbmMiOiJBMjU2Q0JDLUhTNTEyIn0..ewRkmEff3GQym6BLEngEsA.mK0ZFEmxcEIwBHieyLfVhhY-O36ziwyv8FSXwaaZ1kT9Xa7m38hhcpAsr9nKl35zHIBxyhDrRVEq49tOuKP7I0scKjsT-3Msux-zLkEuXpeswYR4dD3p6C_dWbLcFtuYo4ir8oN7iNGglG8ujzNXPHIrdO-CuGbhCFU5VLsXB3uHSg3JzpcM9hf9k1HYqC6ajgjp8Ej4v5lCpURbckzNww.efPY1iGBQv9rov1VbSCLxEDJkrbFoFcVMj1gR_LB92w");
        expectedToken.setExpiresIn(600L);
        expectedToken.setScope("");
        expectedToken.setTokenType("Bearer");
        assertThat(accessToken).isEqualTo(expectedToken);
    }
}
