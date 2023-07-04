package com.yolt.creditscoring.service.yoltapi.service;

import com.yolt.creditscoring.controller.user.site.SiteViewDTO;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.yoltapi.dto.*;
import com.yolt.creditscoring.service.yoltapi.http.CreateUserSiteForm;
import com.yolt.creditscoring.service.yoltapi.http.FilledInFormValues;
import com.yolt.creditscoring.service.yoltapi.http.UserConsentParams;
import com.yolt.creditscoring.service.yoltapi.http.YoltHttpClient;
import com.yolt.creditscoring.service.yoltapi.http.model.*;
import com.yolt.creditscoring.service.yoltapi.webclient.ClientAuthenticationMeans;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class YoltFetchDataService {

    private final YoltHttpClient yoltHttpClient;

    public List<SiteViewDTO> fetchSites(ClientAuthenticationMeans authenticationMean, String siteTags) {
        String[] siteTagsList = siteTags.split(",");

        Set<ClientSiteEntity> clientSiteEntities = new LinkedHashSet<>();

        for (String siteTag : siteTagsList) {
            ClientSiteEntity[] sites = yoltHttpClient.getClientSite(authenticationMean, siteTag);
            if (ArrayUtils.isNotEmpty(sites)) {
                clientSiteEntities.addAll(Arrays.asList(sites));
            }
        }

        return clientSiteEntities.stream()
                .map(yoltSiteV2 -> SiteViewDTO.builder()
                        .name(yoltSiteV2.getName())
                        .id(yoltSiteV2.getId())
                        .build())
                .toList();
    }

    public UUID createUser(ClientAuthenticationMeans authenticationMean) {
        return yoltHttpClient.createUser(authenticationMean).getId();
    }

    public ConsentStep requestUserConsent(ClientAuthenticationMeans authenticationMean, UserConsentParams userConsentParams) {
        LoginStep loginStep = yoltHttpClient.requestUserConsent(authenticationMean, userConsentParams);

        if (checkIfBarclaysMultiFormFlow(loginStep)) {
            log.info("Barclays multi form confirmed");
            CreateUserSiteForm createUserSiteForm = new CreateUserSiteForm(
                    loginStep.getForm().getStateId(),
                    Collections.singletonList(new FilledInFormValues("AccountType", "BARCLAYS"))
            );

            LoginFormResponse loginFormResponse =
                    yoltHttpClient.createUserSiteAfterDynamicFlow(
                            authenticationMean,
                            userConsentParams.getUserId(),
                            createUserSiteForm,
                            userConsentParams.getPsuIpAddress());

            return ConsentStep.builder()
                    .redirectUrl(loginFormResponse.getStep().getRedirect().getUrl())
                    .userSiteId(loginFormResponse.getUserSiteId())
                    .build();
        }

        return ConsentStep.builder()
                .redirectUrl(loginStep.getRedirect().getUrl())
                .userSiteId(loginStep.getUserSiteId())
                .build();
    }

    public LoginResponse createUserSite(ClientAuthenticationMeans authenticationMeans, UUID yoltUserId, String redirectUrl, String userIpAddress) {
        LoginFormResponse userSite = yoltHttpClient.createUserSite(authenticationMeans, yoltUserId, redirectUrl, userIpAddress);

        return LoginResponse.builder()
                .activityId(userSite.getActivityId())
                .redirectUrl(mapMultiStepRedirectUrl(userSite))
                .dataFetchFailureReason(mapDataFetchFailureReason(userSite))
                .build();
    }

    /**
     * Workaround for Barclays dynamic form flow.
     * We are currently supporting only NL and UK banks and Barclays is the only bank from that group that uses dynamic flow.
     * This method checks if the create user-site response will return the Barclays dynamic form response.
     * If yes we will automatically select Barclays in response.
     *
     * @return true if loginStep contains Barclays form, false if not
     */
    private boolean checkIfBarclaysMultiFormFlow(LoginStep loginStep) {
        Optional<List<FormComponentDTO>> formComponents = Optional.of(loginStep)
                .map(LoginStep::getForm)
                .map(FormStepObject::getFormComponents);

        if (formComponents.isPresent()) {
            List<FormComponentDTO> formComponentDTOS = formComponents.get();
            if (formComponentDTOS.size() == 1 && formComponentDTOS.get(0) instanceof SELECT) {
                SelectOptionValueDTO selectOptionValueDTO = new SelectOptionValueDTO();
                selectOptionValueDTO.setValue("BARCLAYS");
                selectOptionValueDTO.setDisplayName("Barclays");
                return ((SELECT) formComponentDTOS.get(0)).getSelectOptionValues().contains(selectOptionValueDTO);
            }
        }
        return false;
    }

    private String mapMultiStepRedirectUrl(LoginFormResponse userSite) {
        if (userSite.getActivityId() == null &&
                ConnectionStatusEnum.STEP_NEEDED.equals(userSite.getUserSite().getConnectionStatus())) {
            return Optional.of(userSite)
                    .map(LoginFormResponse::getStep)
                    .map(Step::getRedirect)
                    .map(RedirectStepObject::getUrl)
                    .orElseThrow(() -> new RuntimeException("Missing redirect url in multi step flow"));
        }
        return null;
    }

    private String mapDataFetchFailureReason(LoginFormResponse userSite) {
        if (userSite.getActivityId() == null &&
                ConnectionStatusEnum.DISCONNECTED.equals(userSite.getUserSite().getConnectionStatus())) {
            return Optional.of(userSite)
                    .map(LoginFormResponse::getUserSite)
                    .map(UserSite::getLastDataFetchFailureReason)
                    .map(LastDataFetchFailureReasonEnum::toString)
                    .orElse(null);
        }
        return null;
    }

    public void removeUser(@NonNull ClientAuthenticationMeans authenticationMeans, @NonNull UUID yoltUserId) {
        yoltHttpClient.removeUser(authenticationMeans, yoltUserId);
    }

    public ConnectionStatus getUserSiteStatus(ClientAuthenticationMeans authenticationMeans,
                                              UUID yoltUserId,
                                              UUID yoltUserSiteId) {
        UserSite siteStatus = yoltHttpClient.getUserSiteStatus(authenticationMeans, yoltUserId, yoltUserSiteId);
        return ConnectionStatus.builder()
                .connectionStatus(siteStatus.getConnectionStatus().name())
                .lastDataFetchTime(siteStatus.getLastDataFetchTime())
                .build();
    }

    public boolean hasDataLoaded(ClientAuthenticationMeans authenticationMeans, UUID yoltUserId, UUID activityId) {
        ActivitiesDTO activities = yoltHttpClient.getUserActivities(authenticationMeans, yoltUserId);

        return activities.getActivities().stream()
                .filter(activity -> activityId.equals(activity.getActivityId()))
                .findFirst()
                .map(creationActivity -> creationActivity.getEndTime() != null)
                .orElse(false);

    }

    public List<CreditScoreAccountDTO> accounts(ClientAuthenticationMeans authenticationMeans, UUID yoltUserId) {
        AccountDTO[] accounts = yoltHttpClient.accounts(authenticationMeans, yoltUserId);
        return Stream.of(accounts)
                .map(accountDTO -> CreditScoreAccountDTO.builder()
                        .id(accountDTO.getId())
                        .balance(accountDTO.getBalance())
                        .lastDataFetchTime(accountDTO.getLastDataFetchTime())
                        .currency(accountDTO.getCurrency().name())
                        .status(accountDTO.getStatus().name())
                        .type(accountDTO.getType().name())
                        .usage(accountDTO.getUsage() == null ? null : accountDTO.getUsage().name())
                        .accountReference(createAccountReference(accountDTO))
                        .creditLimit(getCreditLimitForAccount(accountDTO))
                        .accountHolder(accountDTO.getAccountHolder())
                        .build())
                .toList();
    }

    private AccountReference createAccountReference(AccountDTO accountDTO) {
        if (accountDTO.getAccountReferences() != null) {
            return AccountReference.builder()
                    .iban(accountDTO.getAccountReferences().getIban())
                    .bban(accountDTO.getAccountReferences().getBban())
                    .sortCodeAccountNumber(accountDTO.getAccountReferences().getSortCodeAccountNumber())
                    .maskedPan(accountDTO.getAccountReferences().getMaskedPan())
                    .build();
        } else {
            log.warn("Account without account number! Will be filtered out");
            return AccountReference.builder().build();
        }
    }

    private BigDecimal getCreditLimitForAccount(AccountDTO accountDTO) {
        if (accountDTO.getCurrentAccount() != null && accountDTO.getCurrentAccount().getCreditLimit() != null) {
            return accountDTO.getCurrentAccount().getCreditLimit().abs().negate();
        } else if (accountDTO.getCreditCardAccount() != null && accountDTO.getCreditCardAccount().getCreditLimit() != null) {
            return accountDTO.getCreditCardAccount().getCreditLimit().abs().negate();
        } else {
            return null;
        }
    }

    public List<CreditScoreTransactionDTO> getTransactions(ClientAuthenticationMeans authenticationMeans, UUID yoltUserId, UUID accountId) {
        TransactionsPageDTO transactionsPageDTO = yoltHttpClient.getTransactions(authenticationMeans, yoltUserId, accountId);

        List<TransactionDTO> allTransactionsForGivenAccount = new ArrayList<>(transactionsPageDTO.getTransactions());

        while (fetchingAnotherTransactionIsPossible(transactionsPageDTO)) {
            transactionsPageDTO = yoltHttpClient.getTransactions(
                    authenticationMeans, yoltUserId, accountId, transactionsPageDTO.getNext());
            allTransactionsForGivenAccount.addAll(transactionsPageDTO.getTransactions());
        }

        if (allTransactionsForGivenAccount.stream()
                .anyMatch(transaction -> StringUtils.isBlank(getSMECategoryFromTransaction(transaction)))) {
            log.warn("There are some empty categories returned from Yolt API");
        }

        return allTransactionsForGivenAccount.stream()
                .map(transaction -> CreditScoreTransactionDTO.builder()
                        .amount(transaction.getAmount())
                        .currency(transaction.getCurrency().name())
                        //For the report we want to always use the booking date from the transaction.
                        //Usually the 'date' field is being mapped as 'bookingDate' in providers, however there are
                        //some providers were the 'date' is mapped from different field for some business reasons.
                        //As the 'bookingDate' is a optional field, we need to provide fallback to 'date'.
                        .date(transaction.getBookingDate() != null ? transaction.getBookingDate() : transaction.getDate())
                        .creditScoreTransactionCategory(
                                Category.fromString(getSMECategoryFromTransaction(transaction), transaction.getAmount())
                        )
                        .cycleId(getTransactionCycleIdIfPresent(transaction))
                        .build())
                .toList();
    }

    public List<@Valid CreditScoreTransactionCycleDTO> getCycleTransactions(@NonNull ClientAuthenticationMeans authenticationMeans,
                                                                            @NonNull UUID yoltUserId) {
        TransactionCyclesDTO cycleTransactions = yoltHttpClient.getCycleTransactions(authenticationMeans, yoltUserId);

        return cycleTransactions.getCycles().stream()
                .map(cycle -> CreditScoreTransactionCycleDTO.builder()
                        .cycleId(cycle.getCycleId())
                        .amount(cycle.getAmount())
                        .cycleType(fromCycleTypeEnum(cycle.getCycleType()))
                        .build())
                .toList();
    }

    private boolean fetchingAnotherTransactionIsPossible(TransactionsPageDTO transactions) {
        return !CollectionUtils.isEmpty(transactions.getTransactions()) && StringUtils.isNotEmpty(transactions.getNext());
    }

    private String getSMECategoryFromTransaction(TransactionDTO transactionDTO) {
        return transactionDTO.getEnrichment() != null ? transactionDTO.getEnrichment().getCategorySME() : null;
    }

    private UUID getTransactionCycleIdIfPresent(TransactionDTO transactionDTO) {
        if (transactionDTO.getEnrichment() != null && transactionDTO.getEnrichment().getCycleId() != null) {
            return transactionDTO.getEnrichment().getCycleId();
        }
        return null;
    }

    private CycleType fromCycleTypeEnum(CycleTypeEnum cycleTypeEnum) {
        return switch (cycleTypeEnum) {
            case DEBITS -> CycleType.DEBIT;
            case CREDITS -> CycleType.CREDIT;
        };
    }
}
