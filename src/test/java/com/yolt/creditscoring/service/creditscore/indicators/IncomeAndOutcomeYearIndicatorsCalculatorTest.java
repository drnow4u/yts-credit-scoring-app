package com.yolt.creditscoring.service.creditscore.indicators;

import com.yolt.creditscoring.service.creditscore.LocalDateConverter;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

class IncomeAndOutcomeYearIndicatorsCalculatorTest {

    private final IncomeAndOutcomeYearIndicatorsCalculator incomeAndOutcomeYearIndicatorsCalculator = new IncomeAndOutcomeYearIndicatorsCalculator();

    @Test
    void shouldCalculateAverageValueForZeroTransaction() {
        // Given
        LocalDate reportFetchTime = LocalDate.now();
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of();

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isZero();
        assertThat(result.getOutgoingTransactionsSize()).isZero();
        assertThat(result.getMonthlyAverageIncome()).isZero();
        assertThat(result.getMonthlyAverageCost()).isZero();
        assertThat(result.getTotalIncomeAmount()).isZero();
        assertThat(result.getTotalOutgoingAmount()).isZero();
        assertThat(result.getAverageIncomeTransactionAmount()).isZero();
        assertThat(result.getAverageOutcomeTransactionAmount()).isZero();
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForOneTransaction(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                      @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(10), 12, 10, "120.00", "240.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(12);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(10);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("24.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForLessThanTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                            @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(10), 12, 10, "20.00", "40.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 12, 10, "60.00", "120.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 12, 10, "30.00", "60.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 12, 10, "10.00", "20.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(48);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(40);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.50"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("6.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForLessThanTwelveMonthsAndSkipCurrentMonth(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                               @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(0), 15, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 10, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 10, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 15, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(30);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(45);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("5.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("7.50"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("60.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("90.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                    @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(12), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(11), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(10), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(6), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 10, 20, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(120);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(240);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("360.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("1.50"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForTwelveMonths2MonthsAgo(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                              @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(14), 15, 25, "25.00", "35.00"),
                monthlyReports(latestTransactionDate.minusMonths(13), 15, 25, "25.00", "35.00"),
                monthlyReports(latestTransactionDate.minusMonths(12), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(11), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(10), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(6), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 20, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(100);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(200);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("16.67"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("25.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("200.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("300.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("1.50"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForTwelveMonthsPlusCurrentMonth(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                    @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(12), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(11), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(10), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(6), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(0), 30, 50, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(120);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(240);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("360.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("1.50"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForMoreThanTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                            @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(18), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(17), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(16), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(15), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(14), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(13), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(12), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(11), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(10), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(6), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 10, 20, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(120);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(240);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("360.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("1.50"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForMoreThanTwelveMonthsPlusCurrent(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                       @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(18), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(17), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(16), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(15), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(14), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(13), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(12), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(11), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(10), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(9), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(8), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(7), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(6), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 10, 20, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(0), 30, 15, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(120);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(240);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("20.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("30.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("240.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("360.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("2.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("1.50"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageValueForTransactionsWithMonthGaps(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                 @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        Set<MonthlyAdminReportDTO> monthlyTransactions = Set.of(
                monthlyReports(latestTransactionDate.minusMonths(18), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(17), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(16), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(15), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(14), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(13), 30, 15, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(12), 20, 40, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(5), 20, 40, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(4), 20, 40, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(3), 20, 40, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(2), 20, 40, "20.00", "30.00"),
                monthlyReports(latestTransactionDate.minusMonths(1), 20, 40, "20.00", "30.00")
        );

        // When
        IncomeAndOutcomeYearIndicatorsDTO result = incomeAndOutcomeYearIndicatorsCalculator.calculateIncomeAndOutcomeYearIndicatorsReport(monthlyTransactions, reportFetchTime);

        // Then
        assertThat(result.getStartDate()).isEqualTo(reportFetchTime.minusMonths(12).withDayOfMonth(1));
        assertThat(result.getEndDate()).isEqualTo(reportFetchTime.minusMonths(1).with(lastDayOfMonth()));
        assertThat(result.getIncomingTransactionsSize()).isEqualTo(120);
        assertThat(result.getOutgoingTransactionsSize()).isEqualTo(240);
        assertThat(result.getMonthlyAverageIncome()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getMonthlyAverageCost()).isEqualTo(new BigDecimal("15.00"));
        assertThat(result.getTotalIncomeAmount()).isEqualTo(new BigDecimal("120.00"));
        assertThat(result.getTotalOutgoingAmount()).isEqualTo(new BigDecimal("180.00"));
        assertThat(result.getAverageIncomeTransactionAmount()).isEqualTo(new BigDecimal("1.00"));
        assertThat(result.getAverageOutcomeTransactionAmount()).isEqualTo(new BigDecimal("0.75"));
    }

    private MonthlyAdminReportDTO monthlyReports(LocalDate transactionDate, int incomingSize, int outgoingSize, String totalIncoming, String totalOutgoing) {
        return MonthlyAdminReportDTO.builder()
                .month(transactionDate.getMonthValue())
                .year(transactionDate.getYear())
                .incomingTransactionsSize(incomingSize)
                .totalIncoming(new BigDecimal(totalIncoming))
                .outgoingTransactionsSize(outgoingSize)
                .totalOutgoing(new BigDecimal(totalOutgoing))
                .build();
    }

}
