package com.yolt.creditscoring.service.user.model;

import com.yolt.creditscoring.configuration.validation.constraints.CreditScoreUserName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = CreditScoreUser.TABLE_NAME)
@Entity
@Accessors(chain = true)
public class CreditScoreUser {

    public static final String TABLE_NAME = "credit_score_user";

    @Id
    private UUID id;

    @CreditScoreUserName
    private String name;

    @Email
    private String email;

    @NotNull
    private UUID clientId;

    private UUID clientEmailId;

    /**
     * Inviting client admin e-mail address.
     * The live cycle of report can be longer than client admin.
     * That why amin e-mail is used instead of reference.
     *
     */
    @Email
    @NotNull
    private String adminEmail;

    @Enumerated(EnumType.STRING)
    @NotNull
    private InvitationStatus status;

    @NotNull
    private OffsetDateTime dateTimeInvited;

    private OffsetDateTime dateTimeStatusChange;

    @NotNull
    @Size(max = 60)
    private String invitationHash;

    @NotNull
    private boolean consent;

    private OffsetDateTime dateTimeConsent;

    private UUID termsAndConditionId;

    private UUID privacyPolicyId;

    private String ipAddress;

    private String userAgent;

    private UUID yoltUserId;

    private UUID yoltUserSiteId;

    private UUID yoltActivityId;

    private UUID selectedAccountId;
}
