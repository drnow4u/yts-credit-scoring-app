package com.yolt.creditscoring.service.yoltapi;

import com.yolt.creditscoring.controller.user.site.SiteViewDTO;
import com.yolt.creditscoring.service.securitymodule.vault.VaultSecretKeyService;
import com.yolt.creditscoring.service.yoltapi.configuration.YoltApiProperties;
import com.yolt.creditscoring.service.yoltapi.dto.*;
import com.yolt.creditscoring.service.yoltapi.http.UserConsentParams;
import com.yolt.creditscoring.service.yoltapi.service.YoltAuthorizationService;
import com.yolt.creditscoring.service.yoltapi.service.YoltFetchDataService;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class YoltProvider {

    private final YoltAuthorizationService yoltAuthorizationService;
    private final YoltFetchDataService yoltFetchDataService;
    private final VaultSecretKeyService secretKeyService;
    private final YoltApiProperties yoltApiProperties;

    private volatile YoltAccessToken accessToken = YoltAccessToken.builder()
            .accessToken("")
            .build();

    public List<SiteViewDTO> getSites(@NonNull String siteTags) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.fetchSites(authenticationMeans, siteTags);
    }

    public UUID createUser() {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.createUser(authenticationMeans);
    }

    public ConsentStep requestUserConsent(@NonNull UUID yoltUserId, @NonNull UUID siteId, String psuIpAddress) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        UserConsentParams consentParams = UserConsentParams.builder()
                .psuIpAddress(psuIpAddress)
                .siteId(siteId)
                .userId(yoltUserId)
                .build();
        return yoltFetchDataService.requestUserConsent(authenticationMeans, consentParams);
    }

    public LoginResponse createUserSite(@NonNull UUID yoltUserId, @NonNull String redirectUrl, String userIpAddress) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.createUserSite(authenticationMeans, yoltUserId, redirectUrl, userIpAddress);
    }

    public void removeUser(@NonNull UUID yoltUserId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        yoltFetchDataService.removeUser(authenticationMeans, yoltUserId);
    }

    public @Valid CreditScoreAccountDTO getAccountForCreditScoreCalculations(@NonNull UUID yoltUserId, @NonNull UUID accountId) {
        CreditScoreAccountDTO account = getAccounts(yoltUserId).stream()
                .filter(creditScoreAccountDTO -> accountId.equals(creditScoreAccountDTO.getId()))
                .findFirst()
                .orElseThrow();

        List<CreditScoreTransactionDTO> transactions = getTransactions(yoltUserId, accountId);

        return account.toBuilder().transactions(transactions).build();
    }

    public ConnectionStatus getUserSiteStatus(@NonNull UUID yoltUserId, @NonNull UUID yoltUserSiteId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.getUserSiteStatus(authenticationMeans, yoltUserId, yoltUserSiteId);
    }

    public boolean hasUserDataLoadedCompletely(@NonNull UUID yoltUserId, @NonNull UUID activityId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.hasDataLoaded(authenticationMeans, yoltUserId, activityId);
    }

    public List<CreditScoreAccountDTO> getAccounts(@NonNull UUID yoltUserId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.accounts(authenticationMeans, yoltUserId);

    }

    public List<CreditScoreTransactionDTO> getTransactions(@NonNull UUID yoltUserId, @NonNull UUID accountId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.getTransactions(authenticationMeans, yoltUserId, accountId);
    }

    public List<@Valid CreditScoreTransactionCycleDTO> getCycleTransactions(@NonNull UUID yoltUserId) {
        checkToken();

        ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
        return yoltFetchDataService.getCycleTransactions(authenticationMeans, yoltUserId);
    }

    private void checkToken() {
        if (accessToken == null || accessToken.isTokenExpired()) {
            synchronized(this) {
                if (accessToken == null || accessToken.isTokenExpired()) {
                    ClientAuthenticationMeans authenticationMeans = createClientAuthenticationMeans();
                    accessToken = yoltAuthorizationService.createToken(authenticationMeans);
                }
            }
        }
    }

    private ClientAuthenticationMeans createClientAuthenticationMeans() {
        return ClientAuthenticationMeans.builder()
                .clientId(yoltApiProperties.getClientId())
                .requestTokenPublicKeyId(yoltApiProperties.getRequestTokenPublicKeyId())
                .redirectUrlId(yoltApiProperties.getRedirectUrlId())
                .signingPrivateKey(secretKeyService.getSigningPrivateKey())
                .accessToken(accessToken.getAccessToken())
                .build();
    }

}
