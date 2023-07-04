package com.yolt.creditscoring.service.yoltapi.service;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.service.yoltapi.dto.YoltAccessToken;
import com.yolt.creditscoring.service.yoltapi.exception.TokenCreateException;
import com.yolt.creditscoring.service.yoltapi.http.YoltHttpClient;
import com.yolt.creditscoring.service.yoltapi.http.model.AccessTokenResponse;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class YoltAuthorizationService {

    private static final Clock clock = ClockConfig.getClock();

    private final YoltHttpClient yoltHttpClient;

    public YoltAccessToken createToken(ClientAuthenticationMeans clientAuthenticationMeans) {
        try {
            AccessTokenResponse accessToken = yoltHttpClient.getAccessToken(clientAuthenticationMeans);
            return YoltAccessToken.builder()
                    .accessToken(accessToken.getAccessToken())
                    .expiresIn(accessToken.getExpiresIn())
                    .scope(accessToken.getScope())
                    .tokenType(accessToken.getTokenType())
                    .tokenExpirationTime(Instant.now(clock)
                            .plusSeconds(accessToken.getExpiresIn())
                            .minusSeconds(10)) // adding time buffer for the refresh
                    .build();
        } catch (Exception e) {
            throw new TokenCreateException("Error occur when creating token " + e.getMessage());
        }

    }
}
