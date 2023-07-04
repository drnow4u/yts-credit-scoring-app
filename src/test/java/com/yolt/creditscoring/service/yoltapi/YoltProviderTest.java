package com.yolt.creditscoring.service.yoltapi;

import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.yoltapi.configuration.YoltApiProperties;
import com.yolt.creditscoring.service.yoltapi.dto.YoltAccessToken;
import com.yolt.creditscoring.service.yoltapi.service.YoltAuthorizationService;
import com.yolt.creditscoring.service.yoltapi.service.YoltFetchDataService;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.yolt.creditscoring.TestUtils.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class YoltProviderTest {

    @Mock
    private YoltAuthorizationService authorizationService;

    @Mock
    private YoltFetchDataService fetchDataService;

    @Mock
    private VaultSecretKeyService secretKeyService;

    private YoltProvider yoltProvider;

    @Mock
    private KeyStore trustKeyStore;

    @Mock
    private PrivateKey signingPrivateKey;

    @Mock
    private YoltApiProperties properties;

    @BeforeEach
    void setUp() {
        when(secretKeyService.getSigningPrivateKey()).thenReturn(signingPrivateKey);

        when(properties.getClientId()).thenReturn(SOME_YOLT_CLIENT_ID);
        when(properties.getRequestTokenPublicKeyId()).thenReturn(SOME_CLIENT_REQUEST_TOKEN_PUBLIC_KEY_ID);
        when(properties.getRedirectUrlId()).thenReturn(SOME_CLIENT_REDIRECT_URL_ID);

        yoltProvider = new YoltProvider(authorizationService, fetchDataService, secretKeyService, properties);
    }

    @Test
    void shouldAcquireNewToken() {
        // Given
        YoltAccessToken token = YoltAccessToken.builder()
                .expiresIn(1L)
                .tokenExpirationTime(Instant.now().plus(10L, ChronoUnit.SECONDS))
                .accessToken("New token")
                .build();

        when(authorizationService.createToken(any())).thenReturn(token);

        // When
        yoltProvider.getSites(SOME_CLIENT_SITE_TAGS);

        // Then
        verify(authorizationService).createToken(any());

        ClientAuthenticationMeans authenticationMeans = ClientAuthenticationMeans.builder()
                .accessToken(token.getAccessToken())
                .signingPrivateKey(signingPrivateKey)
                .clientId(SOME_YOLT_CLIENT_ID)
                .requestTokenPublicKeyId(SOME_CLIENT_REQUEST_TOKEN_PUBLIC_KEY_ID)
                .redirectUrlId(SOME_CLIENT_REDIRECT_URL_ID)
                .build();
        verify(fetchDataService).fetchSites(authenticationMeans, SOME_CLIENT_SITE_TAGS);
    }

    @Test
    void shouldAcquireNewTokenWhenPreviousExpired() {
        // Given
        YoltAccessToken expiredToken = YoltAccessToken.builder()
                .expiresIn(1L)
                .tokenExpirationTime(Instant.now().minus(100L, ChronoUnit.SECONDS))
                .accessToken("Expired token")
                .build();

        ReflectionTestUtils.setField(yoltProvider, "accessToken", expiredToken);

        YoltAccessToken token = YoltAccessToken.builder()
                .expiresIn(1L)
                .tokenExpirationTime(Instant.now().plus(10L, ChronoUnit.SECONDS))
                .accessToken("New token")
                .build();

        when(authorizationService.createToken(any())).thenReturn(token);

        // When
        yoltProvider.getSites(SOME_CLIENT_SITE_TAGS);

        // Then
        verify(authorizationService).createToken(any());

        ClientAuthenticationMeans authenticationMeans = ClientAuthenticationMeans.builder()
                .accessToken(token.getAccessToken())
                .signingPrivateKey(signingPrivateKey)
                .clientId(SOME_YOLT_CLIENT_ID)
                .requestTokenPublicKeyId(SOME_CLIENT_REQUEST_TOKEN_PUBLIC_KEY_ID)
                .redirectUrlId(SOME_CLIENT_REDIRECT_URL_ID)
                .build();
        verify(fetchDataService).fetchSites(authenticationMeans, SOME_CLIENT_SITE_TAGS);
    }

    @Test
    void shouldUseAlreadyAcquiredToken() {
        // Given
        YoltAccessToken token = YoltAccessToken.builder()
                .expiresIn(1L)
                .tokenExpirationTime(Instant.now().plus(10L, ChronoUnit.SECONDS))
                .accessToken("New token")
                .build();

        ReflectionTestUtils.setField(yoltProvider, "accessToken", token);

        // When
        yoltProvider.getSites(SOME_CLIENT_SITE_TAGS);

        // Then
        verify(authorizationService, never()).createToken(any());

        ClientAuthenticationMeans authenticationMeans = ClientAuthenticationMeans.builder()
                .accessToken(token.getAccessToken())
                .signingPrivateKey(signingPrivateKey)
                .clientId(SOME_YOLT_CLIENT_ID)
                .requestTokenPublicKeyId(SOME_CLIENT_REQUEST_TOKEN_PUBLIC_KEY_ID)
                .redirectUrlId(SOME_CLIENT_REDIRECT_URL_ID)
                .build();
        verify(fetchDataService).fetchSites(authenticationMeans, SOME_CLIENT_SITE_TAGS);
    }
}
