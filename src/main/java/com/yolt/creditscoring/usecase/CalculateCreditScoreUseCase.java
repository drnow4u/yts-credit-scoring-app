package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.algorithm.CreditScoreAlgorithm;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.MonthlyRecurringTransactionsDTO;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsStorageService;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.TotalRecurringTransactionsAggregator;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.estimate.provider.EstimateProvider;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import com.yolt.creditscoring.service.estimate.storage.EstimateStorageService;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.securitymodule.signature.SignatureService;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionCycleDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CalculateCreditScoreUseCase {

    private final YoltProvider yoltProvider;
    private final EstimateProvider estimateProvider;
    private final CreditScoreStorageService creditScoreStorageService;
    private final UserStorageService userStorageService;
    private final CreditScoreAlgorithm creditScoreAlgorithm;
    private final UserJourneyService userJourneyService;
    private final SignatureService signatureService;
    private final ClientStorageService clientService;
    private final UserAuditService userAuditService;
    private final TotalRecurringTransactionsAggregator totalRecurringTransactionsAggregator;
    private final RecurringTransactionsStorageService recurringTransactionsStorageService;
    private final EstimateStorageService estimateStorageService;

    /**
     * From Yolt API fetching transaction for given userId, calculate report and store report in DB.
     *
     * @param userId credit score user
     * @return false if data fetch was not possible, true if data fetched
     */
    @Transactional
    public boolean calculateCreditReportForGivenAccount(@NonNull UUID userId) {
        var user = userStorageService.findById(userId);

        if (!yoltProvider.hasUserDataLoadedCompletely(user.getYoltUserId(), user.getYoltActivityId()))
            return false;

        userAuditService.logAccountSelected(user.getClientId(), user.getId(), user.getSelectedAccountId());
        try {
            CreditScoreAccountDTO account = yoltProvider.getAccountForCreditScoreCalculations(user.getYoltUserId(), user.getSelectedAccountId());

            ReportSaveDTO creditScoreReport = creditScoreAlgorithm.calculateCreditReport(account);

            List<CreditScoreTransactionCycleDTO> cycleTransactions = yoltProvider.getCycleTransactions(user.getYoltUserId());
            Set<MonthlyRecurringTransactionsDTO> monthlyCycleTransactionsReportSaveDTOs =
                    totalRecurringTransactionsAggregator.calculateRecurringTransactions(account.getTransactions(), cycleTransactions);

            if (clientService.checkIfClientHasPDFeatureEnabled(user.getClientId())) {
                ProbabilityOfDefaultStorage pdResult = estimateProvider.calculatePDForReport(account);
                estimateStorageService.save(user.getId(), pdResult);
            }

            creditScoreReport.setUserId(user.getId());
            ReportSignature calculatedSignature = signatureService.sign(creditScoreReport);

            creditScoreStorageService.saveCreditScoreReportForGivenUser(creditScoreReport, calculatedSignature, user.getId());

            UUID creditReportId = creditScoreStorageService.getCreditScoreReportIdByUser(user);
            recurringTransactionsStorageService.saveRecurringTransactionsForReport(creditReportId, monthlyCycleTransactionsReportSaveDTOs);

            userStorageService.complete(userId);

            userJourneyService.registerReportGenerated(user.getClientId(), user.getId());
            userAuditService.logReportCalculated(user.getClientId(), user.getId(), calculatedSignature.getSignature(), calculatedSignature.getKeyId());
        } catch (Exception e) {
            log.error("There was an error when generating user report", e);
            userStorageService.calculationError(user.getId());
        }

        yoltProvider.removeUser(user.getYoltUserId());
        userStorageService.removeYoltUser(user.getId());
        log.info("Report calculated and saved for user");
        return true;
    }
}
