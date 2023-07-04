package com.yolt.creditscoring.service.creditscore.indicators;

import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.utility.reportwindow.ReportWindow;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowBegin;
import static com.yolt.creditscoring.utility.reportwindow.ReportWindow.windowEnd;

@Service
public class IncomeAndOutcomeYearIndicatorsCalculator {

    /**
     * The assumption is that bank provides more than TRANSACTION_WINDOWS_MONTHS_LONG + 1 transactions' months to fetch.
     */
    public IncomeAndOutcomeYearIndicatorsDTO calculateIncomeAndOutcomeYearIndicatorsReport(Set<MonthlyAdminReportDTO> allMonthlyTransactions, LocalDate reportFetchTime) {

        LinkedList<MonthlyAdminReportDTO> twelveFullMonthsOfMonthlyTransactions =
                getTwelveFullMonthsOfTransactions(
                        allMonthlyTransactions,
                        windowBegin(reportFetchTime),
                        windowEnd(reportFetchTime),
                        MonthlyAdminReportDTO::getYear,
                        MonthlyAdminReportDTO::getMonth
                );

        Integer totalIncomingTransactionsSize = 0;
        Integer totalOutgoingTransactionsSize = 0;
        BigDecimal totalIncomeAmount = BigDecimal.ZERO;
        BigDecimal totalOutcomeAmount = BigDecimal.ZERO;

        for (MonthlyAdminReportDTO monthlyTransactions : twelveFullMonthsOfMonthlyTransactions) {
            totalIncomingTransactionsSize += monthlyTransactions.getIncomingTransactionsSize();
            totalOutgoingTransactionsSize += monthlyTransactions.getOutgoingTransactionsSize();
            totalIncomeAmount = totalIncomeAmount.add(monthlyTransactions.getTotalIncoming());
            totalOutcomeAmount = totalOutcomeAmount.add(monthlyTransactions.getTotalOutgoing());
        }

        LocalDate startDateOfTwelveFullMonthsOfMonthlyTransactions = windowBegin(reportFetchTime);
        LocalDate endDateOfTwelveFullMonthsOfMonthlyTransactions = windowEnd(reportFetchTime);

        return IncomeAndOutcomeYearIndicatorsDTO.builder()
                .startDate(startDateOfTwelveFullMonthsOfMonthlyTransactions)
                .endDate(endDateOfTwelveFullMonthsOfMonthlyTransactions)
                .incomingTransactionsSize(totalIncomingTransactionsSize)
                .outgoingTransactionsSize(totalOutgoingTransactionsSize)
                .monthlyAverageIncome(calculateAverageMonthlyAmount(totalIncomeAmount))
                .monthlyAverageCost(calculateAverageMonthlyAmount(totalOutcomeAmount))
                .totalIncomeAmount(totalIncomeAmount.abs())
                .totalOutgoingAmount(totalOutcomeAmount.abs())
                .averageIncomeTransactionAmount(
                        calculateAverageTransactionsAmount(totalIncomeAmount, totalIncomingTransactionsSize))
                .averageOutcomeTransactionAmount(
                        calculateAverageTransactionsAmount(totalOutcomeAmount, totalOutgoingTransactionsSize))
                .build();
    }

    /**
     * @param windowBegin date is included (closed range)
     * @param windowEnd   date is included (closed range)
     */
    public static <T> LinkedList<T> getTwelveFullMonthsOfTransactions(Collection<T> allMonthlyTransactions,
                                                                      LocalDate windowBegin,
                                                                      LocalDate windowEnd,
                                                                      ToIntFunction<T> yearExtractor,
                                                                      ToIntFunction<T> monthExtractor) {

        Assert.isTrue(windowBegin.isBefore(windowEnd), "Date of beginning of the calculation window has to be before end date");

        final Predicate<T> isTransactionAfterWindowTail = (T monthlyAdminReportDTO) -> {
            final LocalDate transactionMonth = LocalDate.of(yearExtractor.applyAsInt(monthlyAdminReportDTO), monthExtractor.applyAsInt(monthlyAdminReportDTO), 1);
            return !transactionMonth.isBefore(windowBegin);
        };

        final Predicate<T> isTransactionBeforeMonthEndClosed = (T monthlyAdminReportDTO) -> {
            final LocalDate transactionMonth = LocalDate.of(yearExtractor.applyAsInt(monthlyAdminReportDTO), monthExtractor.applyAsInt(monthlyAdminReportDTO), 1);
            return !transactionMonth.isAfter(windowEnd);
        };

        return allMonthlyTransactions.stream()
                .filter(isTransactionBeforeMonthEndClosed)
                .filter(isTransactionAfterWindowTail)
                .sorted(Comparator.comparingInt(yearExtractor)
                        .thenComparingInt(monthExtractor))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private BigDecimal calculateAverageMonthlyAmount(BigDecimal amount) {
        return !amount.equals(BigDecimal.ZERO) ?
                amount.divide(BigDecimal.valueOf(ReportWindow.TRANSACTION_WINDOWS_MONTHS_LONG), 2, RoundingMode.HALF_UP).abs()
                : new BigDecimal("0.00");
    }

    private BigDecimal calculateAverageTransactionsAmount(BigDecimal amount, int totalTransactionSize) {
        return totalTransactionSize != 0 ?
                amount.divide(BigDecimal.valueOf(totalTransactionSize), 2, RoundingMode.HALF_UP).abs()
                : new BigDecimal("0.00");
    }
}
