package com.yolt.creditscoring.service.creditscore.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static com.yolt.creditscoring.service.creditscore.model.Category.*;
import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @ParameterizedTest
    @MethodSource("categorySource")
    void shouldCorrectlyMapAllSMECategories(String categoryAsString, Enum categoryAsEnum) {
        // Given
        BigDecimal someValue = new BigDecimal("100");

        // When
        Category result = fromString(categoryAsString, someValue);

        // Then
        assertThat(result).isEqualTo(categoryAsEnum);
    }

    @Test
    void shouldMapUnknownIncomeCategoryToOtherIncomeCategory() {
        // Given
        String someUnknownCategory = "???";

        // When
        Category result = fromString(someUnknownCategory, new BigDecimal("100"));

        // Then
        assertThat(result).isEqualTo(OTHER_INCOME);
    }

    @Test
    void shouldMapUnknownExpenseCategoryToOtherExpensesCategory() {
        // Given
        String someUnknownCategory = "???";

        // When
        Category result = fromString(someUnknownCategory, new BigDecimal("-100"));

        // Then
        assertThat(result).isEqualTo(OTHER_EXPENSES);
    }

    @Test
    void shouldCorrectlyMapNullCategory() {
        // Given
        String nullCategory = null;

        // When
        Category result = fromString(nullCategory, new BigDecimal("-100"));

        // Then
        assertThat(result).isEqualTo(OTHER_EXPENSES);
    }

    private static Stream<Arguments> categorySource() {
        return Stream.of(
                Arguments.of("Loans", LOANS),
                Arguments.of("Equity Financing", EQUITY_FINANCING),
                Arguments.of("Revenue", REVENUE),
                Arguments.of("Tax Returns", TAX_RETURNS),
                Arguments.of("Other Income", OTHER_INCOME),
                Arguments.of("Interest and Repayments", INTEREST_AND_REPAYMENTS),
                Arguments.of("Investments", INVESTMENTS),
                Arguments.of("Food and Drinks", FOOD_AND_DRINKS),
                Arguments.of("Vehicles and Driving Expenses", VEHICLE_AND_DRIVING_EXPENSES),
                Arguments.of("Rent and Facilities", RENT_AND_FACILITIES),
                Arguments.of("Travel Expenses", TRAVEL_EXPENSES),
                Arguments.of("Marketing and Promotion", MARKETING_AND_PROMOTION),
                Arguments.of("Other Operating Costs", OTHER_OPERATING_COSTS),
                Arguments.of("Utilities", UTILITIES),
                Arguments.of("Collection Costs", COLLECTION_COSTS),
                Arguments.of("Salaries", SALARIES),
                Arguments.of("Pension Payments", PENSION_PAYMENTS),
                Arguments.of("Corporate Savings Deposits", CORPORATE_SAVINGS_DEPOSITS),
                Arguments.of("Equity Withdrawal", EQUITY_WITHDRAWAL),
                Arguments.of("Sales Tax", SALES_TAX),
                Arguments.of("Payroll Tax", PAYROLL_TAX),
                Arguments.of("Corporate Income Tax", CORPORATE_INCOME_TAX),
                Arguments.of("Unspecified Tax", UNSPECIFIED_TAX),
                Arguments.of("Other Expenses", OTHER_EXPENSES)
        );
    }


}