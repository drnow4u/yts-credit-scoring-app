package com.yolt.creditscoring.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Clock;
import java.time.ZoneId;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClockConfig {

    @Getter
    private static final Clock clock = Clock.systemUTC();

    @Getter
    private static final Clock amsterdamClock = Clock.system(ZoneId.of("Europe/Amsterdam"));
}
