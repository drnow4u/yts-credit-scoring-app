package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.configuration.annotation.UseCase;
import com.yolt.creditscoring.controller.admin.estimate.FeatureToggleDisableException;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.category.CategoryService;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.indicators.IncomeAndOutcomeYearIndicatorsCalculator;
import com.yolt.creditscoring.service.creditscore.indicators.IncomeAndOutcomeYearIndicatorsDTO;
import com.yolt.creditscoring.service.creditscore.indicators.TaxYearIndicatorsDTO;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.AverageRecurringTransactionCalculator;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.MonthlyRecurringTransactionsDTO;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringAverageDTO;
import com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.OverviewInfoDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.TogglesDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminOverviewResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.service.creditscore.model.Category.*;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowBegin;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowEnd;

@Slf4j
@UseCase
@Validated
@RequiredArgsConstructor
public class ReportOverviewUseCase {

    private final UserStorageService userStorageService;
    private final ClientStorageService clientService;
    private final CreditScoreStorageService creditScoreStorageService;
    private final RecurringTransactionsStorageService cycleTransactionsStorageService;
    private final AverageRecurringTransactionCalculator averageRecurringCalculator;
    private final IncomeAndOutcomeYearIndicatorsCalculator incomeAndOutcomeYearIndicatorsCalculator;
    private final CategoryService categoryService;

    public @Valid CreditScoreAdminOverviewResponseDTO getUserCreditScore(UUID userId, UUID clientId) {
        CreditScoreUserDTO user = userStorageService.findById(userId);
        if (!user.getClientId().equals(clientId)) {
            throw new UserNotFoundException("User not found for given client");
        }

        final TogglesDTO toggles = clientService.getFeatureToggles(clientId);

        if (!toggles.isOverviewFeatureToggle()) {
            throw new FeatureToggleDisableException("Client does not have access to overview feature");
        }

        OverviewInfoDTO overviewInfoDTO = creditScoreStorageService.getCreditScoreOverviewInfoDTO(user);

        final LocalDate reportFetchTime = overviewInfoDTO.getLastDataFetchTime().toLocalDate();
        IncomeAndOutcomeYearIndicatorsDTO incomeAndOutcomeYearIndicatorsDTO = incomeAndOutcomeYearIndicatorsCalculator
                .calculateIncomeAndOutcomeYearIndicatorsReport(creditScoreStorageService.getCreditScoreMonthsDTO(user), reportFetchTime);

        Map<Category, SMECategoryDTO> categoriesForUser =
                categoryService.getCategoriesForUser(userId, windowBegin(reportFetchTime), windowEnd(reportFetchTime));
        TaxYearIndicatorsDTO taxYearIndicatorsDTO = calculateTaxYearIndicatorsReport(categoriesForUser);

        UUID creditReportId = creditScoreStorageService.getCreditScoreReportIdByUser(user);
        List<MonthlyRecurringTransactionsDTO> allCycleTransactions =
                cycleTransactionsStorageService.getMonthlyRecurringTransactionsReportSaveDTOs(creditReportId);
        RecurringAverageDTO recurringAverage = averageRecurringCalculator.calculateAverageRecurringValue(allCycleTransactions, reportFetchTime);

        return CreditScoreAdminOverviewResponseDTO.builder()
                .averageRecurringIncome(recurringAverage.getIncomeAverage())
                .averageRecurringCosts(recurringAverage.getOutcomeAverage())
                .startDate(incomeAndOutcomeYearIndicatorsDTO.getStartDate())
                .endDate(incomeAndOutcomeYearIndicatorsDTO.getEndDate())
                .incomingTransactionsSize(incomeAndOutcomeYearIndicatorsDTO.getIncomingTransactionsSize())
                .outgoingTransactionsSize(incomeAndOutcomeYearIndicatorsDTO.getOutgoingTransactionsSize())
                .monthlyAverageIncome(incomeAndOutcomeYearIndicatorsDTO.getMonthlyAverageIncome())
                .monthlyAverageCost(incomeAndOutcomeYearIndicatorsDTO.getMonthlyAverageCost())
                .totalIncomeAmount(incomeAndOutcomeYearIndicatorsDTO.getTotalIncomeAmount())
                .totalOutgoingAmount(incomeAndOutcomeYearIndicatorsDTO.getTotalOutgoingAmount())
                .averageIncomeTransactionAmount(incomeAndOutcomeYearIndicatorsDTO.getAverageIncomeTransactionAmount())
                .averageOutcomeTransactionAmount(incomeAndOutcomeYearIndicatorsDTO.getAverageOutcomeTransactionAmount())
                .vatTotalPayments(taxYearIndicatorsDTO.getVatTotalPayments())
                .vatAverage(taxYearIndicatorsDTO.getVatAverage())
                .totalCorporateTax(taxYearIndicatorsDTO.getTotalCorporateTax())
                .totalTaxReturns(taxYearIndicatorsDTO.getTotalTaxReturns())
                .build();
    }

    static TaxYearIndicatorsDTO calculateTaxYearIndicatorsReport(Map<Category, SMECategoryDTO> categoriesForUser) {

        return TaxYearIndicatorsDTO.builder()
                .vatTotalPayments(Optional.ofNullable(categoriesForUser.get(SALES_TAX)).map(SMECategoryDTO::getTotalTransactions).orElse(0))
                .vatAverage(Optional.ofNullable(categoriesForUser.get(SALES_TAX)).map(SMECategoryDTO::getAverageTransactionAmount).orElse(BigDecimal.ZERO))
                .totalCorporateTax(Optional.ofNullable(categoriesForUser.get(CORPORATE_INCOME_TAX)).map(SMECategoryDTO::getTotalTransactionAmount).orElse(BigDecimal.ZERO))
                .totalTaxReturns(Optional.ofNullable(categoriesForUser.get(TAX_RETURNS)).map(SMECategoryDTO::getTotalTransactionAmount).orElse(BigDecimal.ZERO))
                .build();
    }
}
