package com.yolt.creditscoring.configuration.security.user;

import com.yolt.creditscoring.configuration.security.PrincipalHavingClientId;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class CreditScoreUserPrincipal implements PrincipalHavingClientId {
    @NonNull
    UUID userId;
    @NonNull
    UUID clientId;
    UUID yoltUserId;
    UUID yoltUserSiteId;
    UUID yoltActivityId;
    InvitationStatus initRequestInvitationStatus;
}
