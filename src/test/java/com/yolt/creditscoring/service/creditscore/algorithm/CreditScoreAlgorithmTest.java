package com.yolt.creditscoring.service.creditscore.algorithm;

import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.BDDAssertions.then;

class CreditScoreAlgorithmTest {

    @RepeatedTest(20)
    void shouldCalculateCreditReportForGivenAccountWithIncomingOnePerMonth() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-20", "50.00", Category.LOANS)
                .transaction("2020-12-22", "50.00", Category.LOANS)
                .transaction("2020-11-11", "50.00", Category.LOANS)
                .transaction("2020-10-11", "50.00", Category.EQUITY_FINANCING)
                .transaction("2020-09-11", "50.00", Category.EQUITY_FINANCING)
                .transaction("2020-08-11", "50.00", Category.EQUITY_FINANCING)
                .transaction("2020-07-11", "50.00", Category.REVENUE)
                .transaction("2020-06-11", "50.00", Category.REVENUE)
                .transaction("2020-05-11", "50.00", Category.REVENUE)
                .transaction("2020-04-11", "50.00", Category.TAX_RETURNS)
                .transaction("2020-03-11", "50.00", Category.TAX_RETURNS)
                .transaction("2020-02-11", "50.00", Category.TAX_RETURNS)
                .transaction("2020-01-11", "50.00", Category.OTHER_INCOME)
                .transaction("2019-12-11", "50.00", Category.OTHER_INCOME)
                .transaction("2019-11-11", "50.00", Category.OTHER_INCOME)
                .transaction("2019-10-11", "50.00", Category.OTHER_INCOME)
                .transaction("2019-09-11", "50.00", Category.OTHER_INCOME)
                .transaction("2019-08-11", "50.00", Category.OTHER_INCOME)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5000.00", "4950.00", "4975.00", "50.00", "0", 1, 0, inCategoryReport(Category.LOANS, "50.00", 1)),
                        inReport(2020, 12, "4950.00", "4900.00", "4925.00", "50.00", "0", 1, 0, inCategoryReport(Category.LOANS, "50.00", 1)),
                        inReport(2020, 11, "4900.00", "4850.00", "4875.00", "50.00", "0", 1, 0, inCategoryReport(Category.LOANS, "50.00", 1)),
                        inReport(2020, 10, "4850.00", "4800.00", "4825.00", "50.00", "0", 1, 0, inCategoryReport(Category.EQUITY_FINANCING, "50.00", 1)),
                        inReport(2020, 9, "4800.00", "4750.00", "4775.00", "50.00", "0", 1, 0, inCategoryReport(Category.EQUITY_FINANCING, "50.00", 1)),
                        inReport(2020, 8, "4750.00", "4700.00", "4725.00", "50.00", "0", 1, 0, inCategoryReport(Category.EQUITY_FINANCING, "50.00", 1)),
                        inReport(2020, 7, "4700.00", "4650.00", "4675.00", "50.00", "0", 1, 0, inCategoryReport(Category.REVENUE, "50.00", 1)),
                        inReport(2020, 6, "4650.00", "4600.00", "4625.00", "50.00", "0", 1, 0, inCategoryReport(Category.REVENUE, "50.00", 1)),
                        inReport(2020, 5, "4600.00", "4550.00", "4575.00", "50.00", "0", 1, 0, inCategoryReport(Category.REVENUE, "50.00", 1)),
                        inReport(2020, 4, "4550.00", "4500.00", "4525.00", "50.00", "0", 1, 0, inCategoryReport(Category.TAX_RETURNS, "50.00", 1)),
                        inReport(2020, 3, "4500.00", "4450.00", "4475.00", "50.00", "0", 1, 0, inCategoryReport(Category.TAX_RETURNS, "50.00", 1)),
                        inReport(2020, 2, "4450.00", "4400.00", "4425.00", "50.00", "0", 1, 0, inCategoryReport(Category.TAX_RETURNS, "50.00", 1)),
                        inReport(2020, 1, "4400.00", "4350.00", "4375.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1)),
                        inReport(2019, 12, "4350.00", "4300.00", "4325.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1)),
                        inReport(2019, 11, "4300.00", "4250.00", "4275.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1)),
                        inReport(2019, 10, "4250.00", "4200.00", "4225.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1)),
                        inReport(2019, 9, "4200.00", "4150.00", "4175.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1)),
                        inReport(2019, 8, "4150.00", "4100.00", "4125.00", "50.00", "0", 1, 0, inCategoryReport(Category.OTHER_INCOME, "50.00", 1))
                );
    }

    @RepeatedTest(20)
    void shouldCalculateCreditReportForGivenAccountWithOutgoingOnePerMonth() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-20", "-50.00", Category.INTEREST_AND_REPAYMENTS)
                .transaction("2020-12-22", "-50.00", Category.INTEREST_AND_REPAYMENTS)
                .transaction("2020-11-11", "-50.00", Category.INTEREST_AND_REPAYMENTS)
                .transaction("2020-10-11", "-50.00", Category.INTEREST_AND_REPAYMENTS)
                .transaction("2020-09-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-08-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-07-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-06-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-05-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-04-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-03-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-02-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2020-01-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2019-12-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2019-11-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2019-10-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2019-09-11", "-50.00", Category.OTHER_EXPENSES)
                .transaction("2019-08-11", "-50.00", Category.OTHER_EXPENSES)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5050.00", "5000.00", "5025.00", "0", "50.00", 0, 1, inCategoryReport(Category.INTEREST_AND_REPAYMENTS, "50.00", 1)),
                        inReport(2020, 12, "5100.00", "5050.00", "5075.00", "0", "50.00", 0, 1, inCategoryReport(Category.INTEREST_AND_REPAYMENTS, "50.00", 1)),
                        inReport(2020, 11, "5150.00", "5100.00", "5125.00", "0", "50.00", 0, 1, inCategoryReport(Category.INTEREST_AND_REPAYMENTS, "50.00", 1)),
                        inReport(2020, 10, "5200.00", "5150.00", "5175.00", "0", "50.00", 0, 1, inCategoryReport(Category.INTEREST_AND_REPAYMENTS, "50.00", 1)),
                        inReport(2020, 9, "5250.00", "5200.00", "5225.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 8, "5300.00", "5250.00", "5275.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 7, "5350.00", "5300.00", "5325.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 6, "5400.00", "5350.00", "5375.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 5, "5450.00", "5400.00", "5425.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 4, "5500.00", "5450.00", "5475.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 3, "5550.00", "5500.00", "5525.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 2, "5600.00", "5550.00", "5575.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2020, 1, "5650.00", "5600.00", "5625.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2019, 12, "5700.00", "5650.00", "5675.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2019, 11, "5750.00", "5700.00", "5725.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2019, 10, "5800.00", "5750.00", "5775.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2019, 9, "5850.00", "5800.00", "5825.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1)),
                        inReport(2019, 8, "5900.00", "5850.00", "5875.00", "0", "50.00", 0, 1, inCategoryReport(Category.OTHER_EXPENSES, "50.00", 1))
                );
    }

    @RepeatedTest(20)
    void calculateCreditReportForGivenAccount() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-25", "-50.00", Category.FOOD_AND_DRINKS)
                .transaction("2021-01-20", "-200.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-13", "-500.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-10", "2000.00", Category.REVENUE)

                .transaction("2020-12-22", "100.00", Category.TAX_RETURNS)
                .transaction("2020-12-13", "-200.00", Category.FOOD_AND_DRINKS)
                .transaction("2020-12-12", "2000.00", Category.REVENUE)
                .transaction("2020-12-10", "-800.00", Category.TRAVEL_EXPENSES)

                .transaction("2020-11-11", "2000.00", Category.REVENUE)
                .transaction("2020-11-02", "-1700.00", Category.UNSPECIFIED_TAX)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5750.00", "3750.00", "4960.00", "2000.00", "750.00", 1, 3,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "50.00", 1),
                                inCategoryReport(Category.OTHER_OPERATING_COSTS, "700.00", 2),
                                inCategoryReport(Category.REVENUE, "2000.00", 1)),
                        inReport(2020, 12, "3850.00", "1850.00", "3150.00", "2100.00", "1000.00", 2, 2,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "200.00", 1),
                                inCategoryReport(Category.REVENUE, "2000.00", 1),
                                inCategoryReport(Category.TAX_RETURNS, "100.00", 1),
                                inCategoryReport(Category.TRAVEL_EXPENSES, "800.00", 1)),
                        inReport(2020, 11, "2650.00", "650.00", "1883.33", "2000.00", "1700.00", 1, 1,
                                inCategoryReport(Category.REVENUE, "2000.00", 1),
                                inCategoryReport(Category.UNSPECIFIED_TAX, "1700.00", 1))
                );

        then(creditScoreReport.getNewestTransactionDate()).isEqualTo(LocalDate.of(2021, 1, 25));
        then(creditScoreReport.getOldestTransactionDate()).isEqualTo(LocalDate.of(2020, 11, 2));
        then(creditScoreReport.getAccountHolder()).isEqualTo("SOME_ACCOUNT_HOLDER");
        then(creditScoreReport.getCreditLimit()).isEqualTo(new BigDecimal("1000"));
        then(creditScoreReport.getTransactionsSize()).isEqualTo(10);
        then(creditScoreReport.getCurrency()).isEqualTo("EUR");
        then(creditScoreReport.getLastDataFetchTime()).isEqualTo(OffsetDateTime.of(2021, 10, 14, 12, 0, 0, 0, ZoneOffset.UTC));
        then(creditScoreReport.getIban()).isEqualTo("SOME_IBAN");
        then(creditScoreReport.getBban()).isEqualTo("SOME_BBAN");
        then(creditScoreReport.getSortCodeAccountNumber()).isEqualTo("SOME_SORT_CODE");
        then(creditScoreReport.getMaskedPan()).isEqualTo("SOME_PAN");
    }

    @RepeatedTest(20)
    void calculateCreditReportForAccountOnlyWithOutgoingTransactions() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-25", "-50.00", Category.FOOD_AND_DRINKS)
                .transaction("2021-01-20", "-200.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-13", "-500.00", Category.OTHER_OPERATING_COSTS)

                .transaction("2020-12-13", "-200.00", Category.FOOD_AND_DRINKS)
                .transaction("2020-12-10", "-800.00", Category.TRAVEL_EXPENSES)

                .transaction("2020-11-02", "-1700.00", Category.UNSPECIFIED_TAX)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5750.00", "5000.00", "5262.50", "0", "750.00", 0, 3,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "50.00", 1),
                                inCategoryReport(Category.OTHER_OPERATING_COSTS, "700.00", 2)),
                        inReport(2020, 12, "6750.00", "5750.00", "6150.00",  "0", "1000.00", 0, 2,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "200.00", 1),
                                inCategoryReport(Category.TRAVEL_EXPENSES, "800.00", 1)),
                        inReport(2020, 11, "8450.00", "6750.00", "7600.00",  "0", "1700.00", 0, 1,
                                inCategoryReport(Category.UNSPECIFIED_TAX, "1700.00", 1))
                );
    }

    @RepeatedTest(20)
    void calculateCreditReportForAccountOnlyWithIncomingTransactions() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-25", "50.00", Category.LOANS)
                .transaction("2021-01-20", "200.00", Category.LOANS)
                .transaction("2021-01-13", "500.00", Category.LOANS)

                .transaction("2020-12-13", "200.00", Category.TAX_RETURNS)
                .transaction("2020-12-10", "800.00", Category.TAX_RETURNS)

                .transaction("2020-11-02", "1700.00", Category.REVENUE)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5000.00", "4250.00", "4737.50", "750.00", "0", 3, 0,
                                inCategoryReport(Category.LOANS, "750.00", 3)),
                        inReport(2020, 12, "4250.00", "3250.00", "3850.00", "1000.00", "0", 2, 0,
                                inCategoryReport(Category.TAX_RETURNS, "1000.00", 2)),
                        inReport(2020, 11, "3250.00", "1550.00", "2400.00", "1700.00", "0", 1, 0,
                                inCategoryReport(Category.REVENUE, "1700.00", 1))
                );

    }

    @RepeatedTest(20)
    void shouldCalculateCreditReportWithTransactionGroupByDaysBalanceZero() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("0.00")
                .transaction("2021-01-25", "-50.00", Category.CORPORATE_INCOME_TAX)
                .transaction("2021-01-25", "50.00", Category.TAX_RETURNS)

                .transaction("2020-12-22", "100.00", Category.TAX_RETURNS)
                .transaction("2020-12-22", "-100.00", Category.CORPORATE_INCOME_TAX)

                .transaction("2020-11-11", "-200.00", Category.CORPORATE_INCOME_TAX)
                .transaction("2020-11-11", "200.00", Category.TAX_RETURNS)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "0.00", "0.00", "0.00", "50.00", "50.00", 1, 1,
                                inCategoryReport(Category.CORPORATE_INCOME_TAX, "50.00", 1),
                                inCategoryReport(Category.TAX_RETURNS, "50.00", 1)),
                        inReport(2020, 12, "0.00", "0.00", "0.00", "100.00", "100.00", 1, 1,
                                inCategoryReport(Category.CORPORATE_INCOME_TAX, "100.00", 1),
                                inCategoryReport(Category.TAX_RETURNS, "100.00", 1)),
                        inReport(2020, 11, "0.00", "0.00", "0.00", "200.00", "200.00", 1, 1,
                                inCategoryReport(Category.CORPORATE_INCOME_TAX, "200.00", 1),
                                inCategoryReport(Category.TAX_RETURNS, "200.00", 1))
                );
    }

    @RepeatedTest(20)
    void shouldCalculateCreditReportWithTransactionGroupByDays() {
        // Credit Score report is not calculated correctly
        // https://yolt.atlassian.net/browse/YTSAPP-169

        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-25", "-50.00", Category.FOOD_AND_DRINKS)
                .transaction("2021-01-20", "-200.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-13", "-500.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-10", "2000.00", Category.REVENUE)

                .transaction("2020-12-22", "100.00", Category.TAX_RETURNS)
                .transaction("2020-12-13", "-200.00", Category.FOOD_AND_DRINKS)
                .transaction("2020-12-12", "2000.00", Category.REVENUE)
                .transaction("2020-12-10", "-800.00", Category.TRAVEL_EXPENSES)

                .transaction("2020-11-11", "600.00", Category.LOANS)
                .transaction("2020-11-11", "500.00", Category.OTHER_INCOME)
                .transaction("2020-11-11", "1200.00", Category.REVENUE)
                .transaction("2020-11-11", "-100.00", Category.UNSPECIFIED_TAX)
                .transaction("2020-11-11", "-200.00", Category.UNSPECIFIED_TAX)
                .transaction("2020-11-02", "-1700.00", Category.UNSPECIFIED_TAX)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5750.00", "3750.00", "4960.00", "2000.00", "750.00", 1, 3,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "50.00", 1),
                                inCategoryReport(Category.OTHER_OPERATING_COSTS, "700.00", 2),
                                inCategoryReport(Category.REVENUE, "2000.00", 1)),
                        inReport(2020, 12, "3850.00", "1850.00", "3150.00", "2100.00", "1000.00", 2, 2,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "200.00", 1),
                                inCategoryReport(Category.REVENUE, "2000.00", 1),
                                inCategoryReport(Category.TAX_RETURNS, "100.00", 1),
                                inCategoryReport(Category.TRAVEL_EXPENSES, "800.00", 1)),
                        inReport(2020, 11, "2650.00", "650.00", "1883.33", "2300.00", "2000.00", 3, 3,
                                inCategoryReport(Category.LOANS, "600.00", 1),
                                inCategoryReport(Category.OTHER_INCOME, "500.00", 1),
                                inCategoryReport(Category.REVENUE, "1200.00", 1),
                                inCategoryReport(Category.UNSPECIFIED_TAX, "2000.00", 3))
                );
    }

    @RepeatedTest(20)
    void shouldCalculateCreditReportWithZeroTransactionInTheMiddleMonth() {
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("5000.00")
                .transaction("2021-01-25", "-50.00", Category.FOOD_AND_DRINKS)
                .transaction("2021-01-20", "-200.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-13", "-500.00", Category.OTHER_OPERATING_COSTS)
                .transaction("2021-01-10", "2000.00", Category.REVENUE)

                .transaction("2020-11-11", "600.00", Category.LOANS)
                .transaction("2020-11-11", "500.00", Category.OTHER_INCOME)
                .transaction("2020-11-11", "1200.00", Category.REVENUE)
                .transaction("2020-11-11", "-100.00", Category.UNSPECIFIED_TAX)
                .transaction("2020-11-11", "-200.00", Category.UNSPECIFIED_TAX)
                .transaction("2020-11-02", "-1700.00", Category.UNSPECIFIED_TAX)
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly())
                .extracting("year", "month", "highestBalance", "lowestBalance", "averageBalance", "totalIncoming", "totalOutgoing", "incomingTransactionsSize", "outgoingTransactionsSize", "categoriesAmounts")
                .containsOnly(
                        inReport(2021, 1, "5750.00", "3750.00", "4960.00", "2000.00", "750.00", 1, 3,
                                inCategoryReport(Category.FOOD_AND_DRINKS, "50.00", 1),
                                inCategoryReport(Category.OTHER_OPERATING_COSTS, "700.00", 2),
                                inCategoryReport(Category.REVENUE, "2000.00", 1)),
                        inReport(2020, 11, "3750.00", "1750.00", "2983.33",  "2300.00", "2000.00", 3, 3,
                                inCategoryReport(Category.LOANS, "600.00", 1),
                                inCategoryReport(Category.OTHER_INCOME, "500.00", 1),
                                inCategoryReport(Category.REVENUE, "1200.00", 1),
                                inCategoryReport(Category.UNSPECIFIED_TAX, "2000.00", 3))
                );
    }

    @Test
    void shouldCalculateCreditReportWithZeroTransactions() {
        // https://yolt.atlassian.net/browse/YTSAPP-214
        // Given
        CreditScoreAlgorithm creditScoreAlgorithm = new CreditScoreAlgorithm();

        CreditScoreUser creditScoreUser = new CreditScoreUser();
        creditScoreUser.setId(SOME_USER_ID);
        creditScoreUser.setYoltUserId(SOME_YOLT_USER_ID);
        creditScoreUser.setYoltUserSiteId(SOME_YOLT_USER_SITE_ID);

        CreditScoreAccountDTO creditScoreAccountDTO = new CreditScoreAccountDTOBuilderTest()
                .balance("0.00")
                .build();

        // When
        var creditScoreReport = creditScoreAlgorithm.calculateCreditReport(creditScoreAccountDTO);

        // Then
        then(creditScoreReport.getCreditScoreMonthly()).isEmpty();
        then(creditScoreReport.getNewestTransactionDate()).isNull();
        then(creditScoreReport.getOldestTransactionDate()).isNull();
        then(creditScoreReport.getTransactionsSize()).isEqualTo(0);
        then(creditScoreReport.getAccountHolder()).isEqualTo("SOME_ACCOUNT_HOLDER");
        then(creditScoreReport.getCreditLimit()).isEqualTo(new BigDecimal("1000"));
        then(creditScoreReport.getCurrency()).isEqualTo("EUR");
        then(creditScoreReport.getLastDataFetchTime()).isEqualTo(OffsetDateTime.of(2021, 10, 14, 12, 0, 0, 0, ZoneOffset.UTC));
        then(creditScoreReport.getIban()).isEqualTo("SOME_IBAN");
        then(creditScoreReport.getBban()).isEqualTo("SOME_BBAN");
        then(creditScoreReport.getSortCodeAccountNumber()).isEqualTo("SOME_SORT_CODE");
        then(creditScoreReport.getMaskedPan()).isEqualTo("SOME_PAN");
    }

    private static Tuple inReport(int year, int month, String highestBalance, String lowestBalance, String averageBalance,
                                  String totalIncoming, String totalOutgoing, int incomingTransactionNumber,
                                  int outgoingTransactionNumber, Tuple... inCategoryReport) {
        return tuple(year,
                month,
                new BigDecimal(highestBalance),
                new BigDecimal(lowestBalance),
                new BigDecimal(averageBalance),
                new BigDecimal(totalIncoming),
                new BigDecimal(totalOutgoing),
                incomingTransactionNumber,
                outgoingTransactionNumber,
                Arrays.stream(inCategoryReport)
                        .map(tuple -> MonthlyCategoryReportSaveDTO.builder()
                                .category((Category) tuple.toList().get(0))
                                .amount((BigDecimal) tuple.toList().get(1))
                                .totalTransactions((int) tuple.toList().get(2))
                                .build()).collect(Collectors.toList()));
    }

    private static Tuple inCategoryReport(Category category, String amount, int transactionTotal) {
        return tuple(category, new BigDecimal(amount), transactionTotal);
    }

    static class CreditScoreAccountDTOBuilderTest {


        private final CreditScoreAccountDTO.CreditScoreAccountDTOBuilder builder;
        List<CreditScoreTransactionDTO> transactions = new ArrayList<>();

        CreditScoreAccountDTOBuilderTest() {
            builder = CreditScoreAccountDTO.builder();
        }

        CreditScoreAccountDTOBuilderTest balance(String balance) {
            builder.balance(new BigDecimal(balance));
            return this;
        }

        CreditScoreAccountDTOBuilderTest transaction(String date, String amount, Category category) {
            transactions.add(CreditScoreTransactionDTO.builder()
                    .amount(new BigDecimal(amount))
                    .date(LocalDate.parse(date))
                    .creditScoreTransactionCategory(category)
                    .build());
            return this;
        }

        CreditScoreAccountDTO build() {
            Collections.shuffle(transactions); // Shuffling transactions, to check if the algorithm is correctly sorting them
            return builder.transactions(transactions)
                    .lastDataFetchTime(OffsetDateTime.of(2021, 10, 14, 12, 0, 0, 0, ZoneOffset.UTC))
                    .currency("EUR")
                    .creditLimit(new BigDecimal("1000"))
                    .accountHolder("SOME_ACCOUNT_HOLDER")
                    .accountReference(AccountReference.builder()
                            .iban("SOME_IBAN")
                            .bban("SOME_BBAN")
                            .sortCodeAccountNumber("SOME_SORT_CODE")
                            .maskedPan("SOME_PAN")
                            .build())
                    .build();
        }

    }

}
