package com.yolt.creditscoring.service.audit;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Map;
import java.util.UUID;

@Builder
@Value
public class UserAuditDTO {
    @NonNull
    UUID userId;
    @NonNull
    UUID clientId;

    @Singular
    Map<String, String> details;
}
