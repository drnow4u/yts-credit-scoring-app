package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.user.UserReportDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.usecase.dto.CreditScoreUserResponseDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.time.Clock;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class ConfirmCreditScoreReportUseCase {

    public static final int YOLT_DEFAULT_FETCH_WINDOW = 18;
    private final UserStorageService userStorageService;
    private final CreditScoreStorageService creditScoreStorageService;
    private final ClientStorageService clientService;
    private final UserJourneyService userJourneyService;
    private final UserAuditService userAuditService;
    private final YoltProvider yoltProvider;

    private final Clock clock = Clock.systemUTC();

    public @Valid CreditScoreUserResponseDTO getReportForUser(UUID userId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);

        InvitationStatus creditScoreUserInvitationStatus = userStorageService.getCreditScoreUserInvitationStatus(userId);
        if (!InvitationStatus.ACCOUNT_SELECTED.equals(creditScoreUserInvitationStatus)) {
            throw new IllegalStateException("Wrong user workflow state");
        }

        String clientAdditionalReportText = clientService.getClientAdditionalReportTextBasedOnClientId(user.getClientId());

        return yoltProvider.getAccounts(user.getYoltUserId()).stream()
                .filter(account -> account.getId().equals(user.getSelectedAccountId()))
                .findFirst()
                .map(account -> mapCreditScoreReportToCreditScoreResponseDTO(user.getId(), account, clientAdditionalReportText, user.getEmail()))
                .orElseThrow(() -> new NoSuchElementException("Previously selected account by user is missing in Yolt API"));
    }

    /**
     * User confirm to share already generated report.
     */
    @Transactional
    public void confirmReportShare(UUID userId, UUID clientId) {
        userStorageService.reportShareConfirmed(userId);
        userJourneyService.registerReportSaved(clientId, userId);
        userAuditService.logConfirmReportShare(clientId, userId);
        log.info("Report share confirmed");
    }

    /**
     * User reject to share already generated report.
     *
     * Report should not be stored if user didn't confirm to share.
     * If such situation happen we will remove such report.
     */
    @Transactional
    public void refuseReportShare(@NonNull UUID userId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);

        if (creditScoreStorageService.findCreditScoreReportIdByUserId(user.getId()).isPresent()) {
            log.error("The credit report should not be stored when user refuse");
            creditScoreStorageService.deleteByCreditScoreUserId(user.getId());
        }

        yoltProvider.removeUser(user.getYoltUserId());
        userStorageService.refuse(user.getId());
        userStorageService.removeYoltUser(userId);
        userJourneyService.registerReportRefused(user.getClientId(), user.getId());
        log.info("Report share refused");
    }

    public String getClientRedirectUrlIfPresent(@NonNull UUID userId, @NonNull UUID clientId, boolean wasReportShared) {
        String baseClientRedirectUrl = clientService.getClientRedirectUrl(clientId);
        String status = wasReportShared ? "confirm" : "refuse";

        return StringUtils.isNotEmpty(baseClientRedirectUrl) ? baseClientRedirectUrl + "?userId=" + userId + "&status=" + status : StringUtils.EMPTY;
    }

    private @Valid CreditScoreUserResponseDTO mapCreditScoreReportToCreditScoreResponseDTO(@NonNull UUID userId,
                                                                                           CreditScoreAccountDTO account,
                                                                                           String clientAdditionalReportText,
                                                                                           @Email String email) {
        final LocalDate newestTransactionDate = account.getLastDataFetchTime() != null
                ? account.getLastDataFetchTime().toLocalDate() : LocalDate.now(clock);

        return CreditScoreUserResponseDTO.builder()
                .report(UserReportDTO.builder()
                        .userId(userId)
                        .iban(account.getAccountReference().getIban())
                        .bban(account.getAccountReference().getBban())
                        .maskedPan(account.getAccountReference().getMaskedPan())
                        .sortCodeAccountNumber(account.getAccountReference().getSortCodeAccountNumber())
                        .initialBalance(account.getBalance())
                        .lastDataFetchTime(account.getLastDataFetchTime())
                        // Newest and oldest transactions date is important to user.
                        // It was calculated based on date of each transaction.
                        // Since we are not fetch transaction on for user overview CFA has to mimic this values.
                        .newestTransactionDate(newestTransactionDate)
                        .oldestTransactionDate(newestTransactionDate.minusMonths(YOLT_DEFAULT_FETCH_WINDOW))
                        .currency(account.getCurrency())
                        .creditLimit(account.getCreditLimit())
                        .accountHolder(account.getAccountHolder())
                        .build())
                .userEmail(email)
                .additionalTextReport(clientAdditionalReportText)
                .build();
    }
}
