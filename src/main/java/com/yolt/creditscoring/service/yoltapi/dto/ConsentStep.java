package com.yolt.creditscoring.service.yoltapi.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class ConsentStep {
    @NonNull
    String redirectUrl;
    @NonNull
    UUID userSiteId;
}
