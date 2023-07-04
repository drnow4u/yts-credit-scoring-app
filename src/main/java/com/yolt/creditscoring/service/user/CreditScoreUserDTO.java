package com.yolt.creditscoring.service.user;

import com.yolt.creditscoring.service.user.model.InvitationStatus;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class CreditScoreUserDTO {

    UUID id;

    UUID clientId;

    UUID clientEmailId;

    UUID yoltUserId;

    UUID yoltUserSiteId;

    @NotNull
    @Email
    String adminEmail;

    String name;

    @Email
    String email;

    InvitationStatus status;

    OffsetDateTime dateTimeInvited;

    OffsetDateTime dateTimeStatusChange;

    public boolean isActiveYoltUser() {
        return yoltUserId != null;
    }

    UUID selectedAccountId;

    UUID yoltActivityId;
}
