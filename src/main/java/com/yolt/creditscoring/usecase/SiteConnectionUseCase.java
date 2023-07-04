package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.controller.user.account.Account;
import com.yolt.creditscoring.controller.user.site.SiteLoginStepDTO;
import com.yolt.creditscoring.controller.user.site.SiteViewDTO;
import com.yolt.creditscoring.exception.UserSiteAlreadyExistException;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.ConnectionStatus;
import com.yolt.creditscoring.service.yoltapi.dto.ConsentStep;
import com.yolt.creditscoring.service.yoltapi.dto.LoginResponse;
import com.yolt.creditscoring.service.yoltapi.exception.SiteAuthenticationException;
import com.yolt.creditscoring.service.yoltapi.exception.SiteCreationException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SiteConnectionUseCase {

    private final UserStorageService userStorageService;
    private final YoltProvider yoltProvider;
    private final UserJourneyService userJourneyService;
    private final ClientStorageService clientService;
    private final UserAuditService userAuditService;

    public List<SiteViewDTO> getSitesForClient(UUID clientId) {
        String clientSiteTags = clientService.getSiteTagsForGivenClient(clientId);
        final List<SiteViewDTO> collect = yoltProvider.getSites(clientSiteTags).stream()
                .map(siteViewDTO -> SiteViewDTO.builder()
                        .id(siteViewDTO.getId())
                        .name(siteViewDTO.getName())
                        .build())
                .toList();
        if (collect.isEmpty()) {
            log.warn("Site list is empty. For client check tag configuration: {}", clientSiteTags);
        }

        return collect;
    }

    @Transactional
    public SiteLoginStepDTO requestUserConsent(UUID userId, UUID yoltUserId, UUID yoltUserSiteId, UUID siteId, String userIpAddress) {

        if (yoltUserSiteId != null) {
            throw new UserSiteAlreadyExistException("User site already exists for given user");
        }

        ConsentStep loginStep = yoltProvider.requestUserConsent(yoltUserId, siteId, userIpAddress);

        userStorageService.updateUserSite(userId, loginStep.getUserSiteId());

        return SiteLoginStepDTO.builder()
                .redirectUrl(loginStep.getRedirectUrl())
                .build();
    }

    public LoginResponse createUserSite(UUID userId, UUID yoltUserId, String redirectUrl, String userIpAddress, @NonNull UUID clientId) {

        LoginResponse userSite = yoltProvider.createUserSite(yoltUserId, redirectUrl, userIpAddress);

        if ("AUTHENTICATION_FAILED".equals(userSite.getDataFetchFailureReason())) {
            userStorageService.refusedBankConsent(userId);
            userJourneyService.registerBankConsentRefused(clientId, userId);
            throw new SiteAuthenticationException("User did not accepted consent on bank page");
        } else if (userSite.getDataFetchFailureReason() != null) {
            userStorageService.bankError(userId);
            userJourneyService.registerBankError(clientId, userId);
            throw new SiteCreationException("UserSite was not created due to: " + userSite.getDataFetchFailureReason());
        }

        if (userSite.getActivityId() != null) {
            userStorageService.updateActivityId(userId, userSite.getActivityId());
        }
        userJourneyService.registerBankConsentAccept(clientId, userId);
        userAuditService.logBankSelected(clientId, userId, userIpAddress);
        return userSite;
    }

    public boolean wasUserSiteDataFetched(UUID userId) {
        CreditScoreUserDTO yoltUser = userStorageService.findById(userId);

        Assert.notNull(yoltUser.getYoltUserId(), "Missing Yolt User Id");
        Assert.notNull(yoltUser.getYoltUserSiteId(), "Missing Yolt User Site Id");

        ConnectionStatus userSiteStatus = yoltProvider.getUserSiteStatus(yoltUser.getYoltUserId(), yoltUser.getYoltUserSiteId());

        return "CONNECTED".equals(userSiteStatus.getConnectionStatus()) && userSiteStatus.getLastDataFetchTime() != null;
    }

    public List<Account> getAccounts(UUID yoltUserId) {
        return yoltProvider.getAccounts(yoltUserId).stream()
                .map(creditScoreAccountDTO -> Account.builder()
                        .id(creditScoreAccountDTO.getId())
                        .accountNumber(getAvailableAccountNumber(creditScoreAccountDTO.getAccountReference()))
                        .balance(creditScoreAccountDTO.getBalance())
                        .currency(creditScoreAccountDTO.getCurrency())
                        .build())
                .filter(account -> account.getAccountNumber() != null)
                .toList();
    }

    private String getAvailableAccountNumber(AccountReference accountReference) {
        if (accountReference.getIban() != null) return accountReference.getIban();
        if (accountReference.getSortCodeAccountNumber() != null) return accountReference.getSortCodeAccountNumber();
        if (accountReference.getBban() != null) return accountReference.getBban();
        if (accountReference.getMaskedPan() != null) return accountReference.getMaskedPan();

        log.warn("Every value in account reference was empty. Account will be filtered out");
        return null;
    }

    public CreditScoreUserDTO updateAccountForUser(@NotNull UUID userId, @NotNull UUID accountId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);

        return yoltProvider.getAccounts(user.getYoltUserId()).stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .map(account -> userStorageService.updateAccountForUser(userId, account.getId()))
                .orElseThrow(() -> new NoSuchElementException("Selected account by user is missing in Yolt API"));
    }
}
