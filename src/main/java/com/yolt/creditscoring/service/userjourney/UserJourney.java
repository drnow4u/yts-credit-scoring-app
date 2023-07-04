package com.yolt.creditscoring.service.userjourney;

import lombok.Builder;
import lombok.Value;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Value
@Builder
public class UserJourney {

    @NotNull
    UUID clientId;

    @NotNull
    UUID userId;

    @Enumerated(EnumType.STRING)
    @NotNull
    JourneyStatus status;

}
