package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import static com.yolt.creditscoring.service.creditscore.indicators.IncomeAndOutcomeYearIndicatorsCalculator.getTwelveFullMonthsOfTransactions;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowBegin;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowEnd;

@Service
public class AverageRecurringTransactionCalculator {

    public static final int TRANSACTION_WINDOWS_MONTHS_LONG = 12;

    /**
     * The assumption is that bank provides more than TRANSACTION_WINDOWS_MONTHS_LONG + 1 transactions' months to fetch.
     *
     * @param allRecurringTransactions
     * @param reportFetchTime
     * @return
     */
    public RecurringAverageDTO calculateAverageRecurringValue(List<MonthlyRecurringTransactionsDTO> allRecurringTransactions, LocalDate reportFetchTime) {

        List<MonthlyRecurringTransactionsDTO> twelveFullMonthsOfRecurringTransactions =
                getTwelveFullMonthsOfTransactions(
                        allRecurringTransactions,
                        windowBegin(reportFetchTime),
                        windowEnd(reportFetchTime),
                        MonthlyRecurringTransactionsDTO::getYear,
                        MonthlyRecurringTransactionsDTO::getMonth);

        BigDecimal totalIncomeRecurringAmount = BigDecimal.ZERO;
        BigDecimal totalOutcomeRecurringAmount = BigDecimal.ZERO;

        for (MonthlyRecurringTransactionsDTO cycleTransactions : twelveFullMonthsOfRecurringTransactions) {
            totalIncomeRecurringAmount = totalIncomeRecurringAmount.add(cycleTransactions.getIncomeRecurringAmount());
            totalOutcomeRecurringAmount = totalOutcomeRecurringAmount.add(cycleTransactions.getOutcomeRecurringAmount());
        }

        BigDecimal averageIncomeRecurringAmount = !totalIncomeRecurringAmount.equals(BigDecimal.ZERO) ?
                totalIncomeRecurringAmount.divide(BigDecimal.valueOf(TRANSACTION_WINDOWS_MONTHS_LONG), 2, RoundingMode.HALF_UP)
                : new BigDecimal("0.00");

        BigDecimal averageOutcomeRecurringAmount = !totalOutcomeRecurringAmount.equals(BigDecimal.ZERO) ?
                totalOutcomeRecurringAmount.divide(BigDecimal.valueOf(TRANSACTION_WINDOWS_MONTHS_LONG), 2, RoundingMode.HALF_UP)
                : new BigDecimal("0.00");

        return RecurringAverageDTO.builder()
                .incomeAverage(averageIncomeRecurringAmount)
                .outcomeAverage(averageOutcomeRecurringAmount)
                .build();
    }

}
