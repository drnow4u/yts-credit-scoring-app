package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import com.yolt.creditscoring.service.creditscore.LocalDateConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AverageRecurringTransactionCalculatorTest {

    private final AverageRecurringTransactionCalculator averageRecurringTransactionCalculator = new AverageRecurringTransactionCalculator();

    @Test
    void shouldCalculateAverageRecurringValueForZeroTransaction() {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of();

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, LocalDate.now());

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("0.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("0.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForOneTransaction(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                               @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "120.00", "240.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForLessThanTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                     @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "30.00", "60.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "30.00", "60.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "30.00", "60.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "30.00", "60.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForLessThanTwelveMonthsAndSkipCurrentMonth(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                                        @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(0), "50.00", "90.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "40.00", "80.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "40.00", "80.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "40.00", "80.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                             @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "10.00", "20.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForTwelveMonths2MonthsAgo(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                       @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(14), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(13), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime.minusMonths(2));

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForTwelveMonthsPlusCurrentMonth(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                             @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(0), "30.00", "50.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForMoreThanTwelveMonths(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                     @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(18), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(17), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(16), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(15), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(14), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(13), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "10.00", "20.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));

    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForMoreThanTwelveMonthsPlusCurrent(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                                @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(18), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(17), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(16), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(15), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(14), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(13), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(0), "30.00", "50.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));

    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForTransactionsWithMonthGaps(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                          @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(18), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(17), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(16), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(15), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(14), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(13), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "20.00", "40.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "20.00", "40.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "20.00", "40.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "20.00", "40.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "20.00", "40.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "20.00", "40.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));

    }

    @ParameterizedTest
    @CsvSource({
            "2021-12-01,             2021-12-01",
            "now(),                  now()"
    })
    void shouldCalculateAverageRecurringValueForMoreThanTwelveMonthsPlusCurrentWhenFetchDateIsTheFirstDayOfMonth(@ConvertWith(LocalDateConverter.class) LocalDate reportFetchTime,
                                                                                                                 @ConvertWith(LocalDateConverter.class) LocalDate latestTransactionDate) {
        // Given
        List<MonthlyRecurringTransactionsDTO> monthlyRecurringTransactions = List.of(
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(18), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(17), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(16), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(15), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(14), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(13), "300.00", "200.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(12), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(11), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(10), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(9), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(8), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(7), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(6), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(5), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(4), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(3), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(2), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(1), "10.00", "20.00"),
                recurringTransactionsMonthsAgo(latestTransactionDate.minusMonths(0), "30.00", "50.00")
        );

        // When
        RecurringAverageDTO result = averageRecurringTransactionCalculator.calculateAverageRecurringValue(monthlyRecurringTransactions, reportFetchTime);

        // Then
        assertThat(result.getIncomeAverage()).isEqualTo(new BigDecimal("10.00"));
        assertThat(result.getOutcomeAverage()).isEqualTo(new BigDecimal("20.00"));
    }

    private MonthlyRecurringTransactionsDTO recurringTransactionsMonthsAgo(LocalDate transactionDate, String incoming, String outgoing) {
        return MonthlyRecurringTransactionsDTO.builder()
                .month(transactionDate.getMonthValue())
                .year(transactionDate.getYear())
                .incomeRecurringAmount(new BigDecimal(incoming))
                .incomeRecurringSize(2)
                .outcomeRecurringAmount(new BigDecimal(outgoing))
                .outcomeRecurringSize(1)
                .build();
    }

}
