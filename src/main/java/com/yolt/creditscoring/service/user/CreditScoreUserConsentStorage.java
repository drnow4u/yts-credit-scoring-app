package com.yolt.creditscoring.service.user;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class CreditScoreUserConsentStorage {

    UUID userId;
    UUID yoltUserId;
    OffsetDateTime dateTimeConsent;
    UUID termsAndConditionId;
    UUID privacyPolicyId;
    String userAddress;
    String userAgent;
}
