package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.controller.user.invitation.ConsentViewDTO;
import com.yolt.creditscoring.exception.InvalidStatusException;
import com.yolt.creditscoring.exception.InvitationExpiredException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.legaldocument.LegalDocumentService;
import com.yolt.creditscoring.service.securitymodule.jwt.JwtCreationService;
import com.yolt.creditscoring.service.user.CreditScoreUserConsentStorage;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class UserInvitationUseCase {

    private static final Clock clock = ClockConfig.getClock();

    private final LegalDocumentService consentService;
    private final YoltProvider yoltProvider;
    private final JwtCreationService jwtCreationService;
    private final UserStorageService userStorageService;
    private final UserJourneyService userJourneyService;
    private final UserAuditService userAuditService;

    public ConsentViewDTO validateUser(String hash) {
        CreditScoreUserDTO user = userStorageService.findByHash(hash)
                .orElseThrow(() -> new UserNotFoundException("User was not found with given hash"));

        if(user.getStatus() != InvitationStatus.INVITED){
            throw new InvitationExpiredException("Expected invited user but was " + user.getStatus());
        }

        if (OffsetDateTime.now(clock).isAfter(user.getDateTimeInvited().plusHours(72))) {
            userStorageService.invitationExpired(user.getId());
            userJourneyService.registerExpired(user.getClientId(), user.getId());
            throw new InvitationExpiredException("The invitation link have expired");
        }

        userAuditService.useInvitationLink(user.getClientId(), user.getId(), user.getEmail());

        return ConsentViewDTO.builder()
                .token(jwtCreationService.createUserToken(hash))
                .build();
    }

    @Transactional
    public void saveLoggedUserConsent(UUID userId, boolean isConsent, String userAgent, String userAddress) {
        CreditScoreUserDTO user = userStorageService.findById(userId);

        if (isConsent) {
            if (userStorageService.isAllowedToSaveUserReport(userId)) {
                throw new InvalidStatusException("Invalid user status for: " + user.getStatus() + " For " + "accepting consent");
            }

            UUID yoltUserid = user.getYoltUserId();
            if (yoltUserid == null) {
                yoltUserid = yoltProvider.createUser();
            }
            boolean isRegistered = userJourneyService.isConsentGeneratedRegistered(user.getClientId(), user.getId());

            UUID termsAndConditionId = consentService.getCurrentTermsAndConditions().getId();
            UUID privacyPolicyId = consentService.getCurrentPrivacyPolicy().getId();
            CreditScoreUserConsentStorage creditScoreUserStorage = CreditScoreUserConsentStorage.builder()
                    .userId(userId)
                    .yoltUserId(yoltUserid)
                    .termsAndConditionId(termsAndConditionId)
                    .privacyPolicyId(privacyPolicyId)
                    .userAgent(userAgent)
                    .userAddress(userAddress)
                    .dateTimeConsent(OffsetDateTime.now(clock))
                    .build();
            if (!isRegistered) {
                userStorageService.saveUserConsent(creditScoreUserStorage);
                userJourneyService.registerConsentGenerated(user.getClientId(), user.getId());
            }
            userAuditService.logUserConsentInAuditLog(creditScoreUserStorage, user.getEmail(), user.getClientId());
        } else {
            if (userStorageService.isAllowedToSaveUserReport(userId)) {
                throw new InvalidStatusException("Invalid user status for: " + user.getStatus() + " For " + "deny consent");
            }
            userStorageService.saveUserConsentDecline(userId);
            userJourneyService.registerConsentRefuse(user.getClientId(), user.getId());
        }
    }

}
