package com.yolt.creditscoring.service.yoltapi.http;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class UserConsentParams {
    @NonNull
    UUID userId;
    @NonNull
    UUID siteId;

    String psuIpAddress;
}
