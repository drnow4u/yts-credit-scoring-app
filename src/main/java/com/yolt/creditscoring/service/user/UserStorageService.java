package com.yolt.creditscoring.service.user;

import com.yolt.creditscoring.configuration.ClockConfig;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static com.yolt.creditscoring.service.user.model.InvitationStatus.*;

@Service
@Validated
@RequiredArgsConstructor
public class UserStorageService {

    private static final Clock clock = ClockConfig.getClock();

    private final CreditScoreUserRepository creditScoreUserRepository;

    public boolean isInvitationStatusExpired(@NonNull UUID creditScoreUserId) {
        var user = findById(creditScoreUserId);
        return EXPIRED == user.getStatus();
    }

    public boolean isStatusesAllowedForConsentPage(@NonNull UUID userId) {
        var user = findById(userId);
        return switch (user.getStatus()) {
            case INVITED, REFUSED -> true;
            default -> false;
        };
    }

    public boolean isSiteConnectAllowed(UUID userId) {
        var user = findById(userId);
        return switch (user.getStatus()) {
            case INVITED -> true;
            default -> false;
        };
    }

    public boolean isAccountAllowed(UUID userId) {
        var user = findById(userId);
        return switch (user.getStatus()) {
            case INVITED -> true;
            default -> false;
        };
    }

    public boolean isAllowedToSaveUserReport(@NonNull UUID userId) {
        CreditScoreUserDTO user = findById(userId);
        return switch (user.getStatus()) {
            case REPORT_SHARING_REFUSED, COMPLETED, EXPIRED -> true;
            default -> false;
        };
    }

    public boolean isOverviewAllowed(@NonNull UUID userId) {
        var user = findById(userId);
        return switch (user.getStatus()) {
            case ACCOUNT_SELECTED -> true;
            default -> false;
        };
    }

    public CreditScoreUserDTO updateUserSite(@NonNull UUID userId, @NonNull UUID userSiteId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setYoltUserSiteId(userSiteId))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow();
    }

    public CreditScoreUserDTO updateActivityId(@NonNull UUID userId, @NonNull UUID activityId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setYoltActivityId(activityId))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow();
    }

    public CreditScoreUserDTO complete(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .filter(u -> REPORT_SHARED == u.getStatus())
                .map(u -> u.setStatus(COMPLETED)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Complete is not allowed for user"));
    }

    public CreditScoreUserDTO calculationError(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(CALCULATION_ERROR)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Calculation error is not allowed for user"));
    }

    public CreditScoreUserDTO refuse(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(REPORT_SHARING_REFUSED)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Report share refused is not allowed for user"));
    }

    public CreditScoreUserDTO refusedBankConsent(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(REFUSED_BANK_CONSENT)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Bank consent refused is not allowed for user"));
    }

    public CreditScoreUserDTO bankError(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(ERROR_BANK)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Bank error is not allowed for user"));
    }

    public CreditScoreUserDTO invitationExpired(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(EXPIRED)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Expired not allowed for user"));
    }

    public CreditScoreUserDTO updateUserInvitationHashAndSetStatusInvited(@NonNull UUID userId, String hash) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setStatus(INVITED)
                        .setDateTimeInvited(OffsetDateTime.now(clock))
                        .setDateTimeStatusChange(OffsetDateTime.now(clock))
                        .setInvitationHash(hash))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public CreditScoreUserDTO saveUserConsent(CreditScoreUserConsentStorage creditScoreUserStorage) {
        return creditScoreUserRepository.findById(creditScoreUserStorage.getUserId())
                .map(u -> u.setConsent(true)
                        .setStatus(INVITED)
                        .setDateTimeConsent(creditScoreUserStorage.getDateTimeConsent())
                        .setTermsAndConditionId(creditScoreUserStorage.getTermsAndConditionId())
                        .setPrivacyPolicyId(creditScoreUserStorage.getPrivacyPolicyId())
                        .setUserAgent(creditScoreUserStorage.getUserAgent())
                        .setIpAddress(creditScoreUserStorage.getUserAddress())
                        .setYoltUserId(creditScoreUserStorage.getYoltUserId()))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new UserNotFoundException(creditScoreUserStorage.getUserId()));
    }

    public CreditScoreUserDTO saveUserConsentDecline(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(u -> u.setConsent(false)
                        .setStatus(REFUSED)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public InvitationStatus getCreditScoreUserInvitationStatus(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(CreditScoreUser::getStatus)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public @Valid CreditScoreUserDTO findById(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public Optional<CreditScoreUserDTO> findByHash(String hash) {
        return creditScoreUserRepository.findByInvitationHash(hash)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO);
    }

    /**
     * This is only mark user as deleted in Yolt API
     * No database delete is processed.
     */
    public CreditScoreUserDTO removeYoltUser(@NonNull UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .map(user -> user.setYoltUserId(null)
                        .setYoltUserSiteId(null)
                        .setSelectedAccountId(null))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public Page<CreditScoreUserDTO> findAllByClientIdOrderByEmailAsc(@NonNull UUID loggedUserClientId, Pageable pageable) {
        return creditScoreUserRepository.findByClientId(loggedUserClientId, pageable)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO);
    }

    public void deleteById(@NonNull UUID userID) {
        creditScoreUserRepository.deleteById(userID);
    }

    public @Valid CreditScoreUserDTO create(@Valid UnaryOperator<CreditScoreUser> userOperator) {
        final CreditScoreUser user = userOperator.apply(new CreditScoreUser())
                .setStatus(INVITED);
        return mapCreditScoreUserToCreditScoreUserDTO(creditScoreUserRepository.save(user));
    }

    private static CreditScoreUserDTO mapCreditScoreUserToCreditScoreUserDTO(CreditScoreUser user) {
        return CreditScoreUserDTO.builder()
                .id(user.getId())
                .clientId(user.getClientId())
                .clientEmailId(user.getClientEmailId())
                .name(user.getName())
                .email(user.getEmail())
                .status(user.getStatus())
                .dateTimeInvited(user.getDateTimeInvited())
                .yoltUserId(user.getYoltUserId())
                .yoltUserSiteId(user.getYoltUserSiteId())
                .dateTimeStatusChange(user.getDateTimeStatusChange())
                .selectedAccountId(user.getSelectedAccountId())
                .yoltActivityId(user.getYoltActivityId())
                .adminEmail(user.getAdminEmail())
                .build();
    }

    public Optional<CreditScoreUser> findByInvitationHash(String userHash) {
        return creditScoreUserRepository.findByInvitationHash(userHash);
    }

    @Transactional
    public CreditScoreUserDTO updateAccountForUser(@NonNull UUID userId, @NonNull UUID accountId) {
        return creditScoreUserRepository.findById(userId)
                .filter(u -> INVITED == u.getStatus())
                .map(u -> u.setSelectedAccountId(accountId)
                        .setStatus(ACCOUNT_SELECTED))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Select account is not allowed for user"));
    }

    public CreditScoreUserDTO reportShareConfirmed(UUID userId) {
        return creditScoreUserRepository.findById(userId)
                .filter(u -> ACCOUNT_SELECTED == u.getStatus())
                .map(u -> u.setStatus(REPORT_SHARED)
                        .setDateTimeStatusChange(OffsetDateTime.now(clock)))
                .map(creditScoreUserRepository::save)
                .map(UserStorageService::mapCreditScoreUserToCreditScoreUserDTO)
                .orElseThrow(() -> new IllegalStateException("Report share is not allowed for user"));
    }
}
