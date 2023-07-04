package com.yolt.creditscoring.service.yoltapi.dto;

import com.yolt.creditscoring.configuration.ClockConfig;
import lombok.Builder;
import lombok.Value;

import java.time.Clock;
import java.time.Instant;

@Builder
@Value
public class YoltAccessToken {

    private static final Clock clock = ClockConfig.getClock();

    String accessToken;

    Long expiresIn;

    String scope;

    String tokenType;

    Instant tokenExpirationTime;

    public boolean isTokenExpired() {
        return tokenExpirationTime == null || Instant.now(clock).isAfter(tokenExpirationTime);
    }
}
