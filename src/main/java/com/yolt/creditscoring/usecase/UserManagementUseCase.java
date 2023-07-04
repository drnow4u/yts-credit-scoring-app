package com.yolt.creditscoring.usecase;

import brave.baggage.BaggageField;
import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.configuration.security.admin.ClientAccessType;
import com.yolt.creditscoring.controller.admin.users.InviteUserDTO;
import com.yolt.creditscoring.controller.admin.users.ViewUserDTO;
import com.yolt.creditscoring.exception.InviteStillPendingException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientEmailDTO;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.email.EmailService;
import com.yolt.creditscoring.service.email.model.InvitationEmailData;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class UserManagementUseCase {

    private static final Clock clock = ClockConfig.getClock();
    private static final String INVITATION_ENDPOINT = "/consent/";

    private final UserStorageService userStorageService;
    private final EmailService emailService;
    private final ClientStorageService clientService;
    private final UserJourneyService userJourneyService;
    private final YoltProvider yoltProvider;
    private final AdminAuditService adminAuditService;
    private final SemaEventService semaEventService;
    private final Supplier<String> invitationHashSupplier;
    private final BaggageField clientUserBaggage;
    private final BaggageField userSiteIdBaggage;
    private final BaggageField appUserIdBaggage;


    /**
     * @param inviteUserDTO parameters needed to invite user
     * @param clientId
     * @param baseUrl
     * @param adminId
     * @param adminEmail
     */
    @Transactional
    public UUID inviteNewUser(@Valid InviteUserDTO inviteUserDTO,
                              @NonNull UUID clientId,
                              @NotNull String baseUrl,
                              @NonNull UUID adminId,
                              @NonNull String adminEmail,
                              @NonNull ClientAccessType clientAccessType) {
        ClientEmailDTO clientEmail = inviteUserDTO.getClientEmailId() != null ?
                clientService.getClientEmailById(inviteUserDTO.getClientEmailId()) :
                clientService.getOnlyClientEmailByClientId(clientId);

        String invitationHash = invitationHashSupplier.get();

        var user = userStorageService.create(u -> u
                .setId(UUID.randomUUID())
                .setName(inviteUserDTO.getName())
                .setEmail(inviteUserDTO.getEmail())
                .setDateTimeInvited(OffsetDateTime.now(clock))
                .setConsent(false)
                .setClientId(clientId)
                .setClientEmailId(clientEmail.getId())
                .setInvitationHash(invitationHash)
                .setAdminEmail(adminEmail)
        );

        injectUserInBaggage(user);

        final InvitationEmailData creditScoreUserInvitation = InvitationEmailData.builder()
                .clientEmail(clientEmail)
                .recipientEmail(inviteUserDTO.getEmail())
                .userName(inviteUserDTO.getName())
                .clientLogoUrl(createClientLogoUrl(baseUrl, clientId))
                .redirectUrl(createInvitationRedirectUrl(invitationHash, baseUrl))
                .build();

        emailService.sendInvitationForUser(clientId, user.getId(), creditScoreUserInvitation);

        userJourneyService.registerInvited(user.getClientId(), user.getId());
        adminAuditService.inviteNewUser(
                user.getClientId(),
                adminId,
                adminEmail,
                user.getId(),
                inviteUserDTO.getName(),
                inviteUserDTO.getEmail(),
                clientAccessType);
        semaEventService.logUserInvitation(clientId, adminId);

        return user.getId();
    }

    @Transactional
    public void resendUserInvite(@NotNull UUID creditScoreUserId,
                                 @NotNull String baseUrl,
                                 @NonNull UUID clientId,
                                 @NonNull UUID adminId,
                                 @NonNull String adminEmail) {
        CreditScoreUserDTO user = userStorageService.findById(creditScoreUserId);

        if (!userStorageService.isInvitationStatusExpired(creditScoreUserId)) {
            throw new InviteStillPendingException("Cannot resend invitation for not expired status");
        }

        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        injectUserInBaggage(user);

        String invitationHash = invitationHashSupplier.get();

        userStorageService.updateUserInvitationHashAndSetStatusInvited(user.getId(), invitationHash);

        ClientEmailDTO clientEmail = clientService.getClientEmailById(user.getClientEmailId());

        final InvitationEmailData invitationEmailData = InvitationEmailData.builder()
                .clientEmail(clientEmail)
                .recipientEmail(user.getEmail())
                .userName(user.getName())
                .clientLogoUrl(createClientLogoUrl(baseUrl, clientId))
                .redirectUrl(createInvitationRedirectUrl(invitationHash, baseUrl))
                .build();

        emailService.sendInvitationForUser(clientId, creditScoreUserId, invitationEmailData);
        adminAuditService.reinviteUser(
                user.getClientId(),
                adminId,
                adminEmail,
                user.getId(),
                user.getName(),
                user.getEmail());
    }

    public Page<ViewUserDTO> viewAllUsersByClientId(Pageable pageable, @NotNull UUID clientId) {
        return userStorageService
                .findAllByClientIdOrderByEmailAsc(clientId, pageable)
                .map(creditScoreUser ->
                        ViewUserDTO.builder()
                                .userId(creditScoreUser.getId())
                                .email(creditScoreUser.getEmail())
                                .name(creditScoreUser.getName())
                                .dateInvited(creditScoreUser.getDateTimeInvited())
                                .dateStatusUpdated(creditScoreUser.getDateTimeStatusChange())
                                .status(creditScoreUser.getStatus())
                                .adminEmail(creditScoreUser.getAdminEmail())
                                .build());
    }

    @Transactional
    public void deleteUserByUserID(@NonNull UUID userID, @NonNull UUID clientId, @NonNull UUID adminId, @NonNull String adminEmail,
                                   @NonNull ClientAccessType clientAccessType) {
        CreditScoreUserDTO user = userStorageService.findById(userID);

        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        injectUserInBaggage(user);

        if (user.isActiveYoltUser()) {
            try {
                yoltProvider.removeUser(user.getYoltUserId());
                log.info("Deleted user {} by admin {}", user.getYoltUserId(), adminId);
            } catch (Exception e) {
                log.warn("User already deleted in YTS API");
            }
        }

        userStorageService.deleteById(userID);
        adminAuditService.deleteUser(clientId, adminId, adminEmail, userID, user.getEmail(), clientAccessType);
    }

    private void injectUserInBaggage(CreditScoreUserDTO user) {

        appUserIdBaggage.updateValue(user.getId().toString());

        if (user.getYoltUserId() != null) {
            clientUserBaggage.updateValue(user.getYoltUserId().toString());
        }

        if (user.getYoltUserSiteId() != null) {
            userSiteIdBaggage.updateValue(user.getYoltUserSiteId().toString());
        }
    }

    private String createInvitationRedirectUrl(String invitationHash, String baseUrl) {
        return baseUrl + INVITATION_ENDPOINT + invitationHash;
    }

    private String createClientLogoUrl(String baseUrl, UUID clientId) {
        return "%s/clients/%s/logo".formatted(baseUrl, clientId);
    }

}
