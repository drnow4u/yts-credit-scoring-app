package com.yolt.creditscoring.service.creditscore.category;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.creditscore.model.*;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import com.yolt.creditscoring.service.user.model.CreditScoreUserRepository;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.BDDAssertions.then;

@IntegrationTest
class CategoryServiceIT {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CreditScoreUserRepository creditScoreUserRepository;

    @Autowired
    private CreditScoreReportRepository creditScoreReportRepository;

    @AfterEach
    void afterTest() {
        creditScoreUserRepository.deleteAll();
        creditScoreReportRepository.deleteAll();
    }

    @Test
    void shouldFetchCategoriesForUser() {
        //Given
        prepareCreditScoreUsers();

        //When
        Map<Category, SMECategoryDTO> results = categoryService.getCategoriesForUser(SOME_USER_ID, LocalDate.parse("2020-10-01"), LocalDate.parse("2021-01-01"));

        //Then
        then(results)
                .contains(
                        entry(Category.OTHER_EXPENSES, smeCategory(Category.OTHER_EXPENSES, SMECategoryType.OUTGOING, "10000.00", 6, "1666.66")),
                        entry(Category.OTHER_INCOME, smeCategory(Category.OTHER_INCOME, SMECategoryType.INCOMING, "11000.00", 3, "3666.66")),
                        entry(Category.REVENUE, smeCategory(Category.REVENUE, SMECategoryType.INCOMING, "10000.00", 1, "10000.00")),
                        entry(Category.SALES_TAX, smeCategory(Category.SALES_TAX, SMECategoryType.OUTGOING, "1000.00", 3, "333.33"))
                );
    }

    @Test
    void shouldFetchCategoriesForUserWithTransactionOutsideCalculationWindow() {
        //Given
        prepareCreditScoreUsers();

        //When
        Map<Category, SMECategoryDTO> results = categoryService.getCategoriesForUser(SOME_USER_ID, LocalDate.parse("2021-01-01"), LocalDate.parse("2021-03-01"));

        //Then
        then(results)
                .isEmpty();
    }

    private SMECategoryDTO smeCategory(Category category, SMECategoryType type, String amount, int total, String average) {
        return SMECategoryDTO.builder()
                .categoryName(category)
                .categoryType(type)
                .totalTransactionAmount(new BigDecimal(amount))
                .totalTransactions(total)
                .averageTransactionAmount(new BigDecimal(average))
                .build();
    }

    private void prepareCreditScoreUsers() {
        CreditScoreUser user1 = new CreditScoreUser()
                .setId(SOME_USER_ID)
                .setName(SOME_USER_NAME)
                .setEmail(SOME_USER_EMAIL)
                .setDateTimeInvited(SOME_FIXED_TEST_DATE)
                .setDateTimeStatusChange(SOME_FIXED_TEST_DATE)
                .setStatus(InvitationStatus.INVITED)
                .setInvitationHash(SOME_USER_HASH)
                .setClientId(SOME_CLIENT_ID)
                .setYoltUserId(SOME_YOLT_USER_ID)
                .setAdminEmail(SOME_CLIENT_ADMIN_EMAIL);

        creditScoreUserRepository.save(user1);

        CreditScoreReport creditScoreReport = CreditScoreReport.builder()
                .id(SOME_CREDIT_REPORT_ID)
                .accountReference(AccountReference.builder()
                        .iban("NL79ABNA12345678901")
                        .bban("79ABNA12345678901")
                        .sortCodeAccountNumber("9455762838")
                        .maskedPan("1234 **** **** 5678")
                        .build())
                .initialBalance(new BigDecimal("5000.00"))
                .lastDataFetchTime(SOME_FIXED_TEST_DATE)
                .currency("EUR")
                .transactionsSize(100)
                .creditLimit(new BigDecimal("-1000.00"))
                .newestTransactionDate(LocalDate.of(2020, 12, 31))
                .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                .accountHolder("Account Holder")
                .creditScoreUserId(user1.getId())
                .signatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .signatureJsonPaths(List.of("$['userId']"))
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReportDecember2020 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2020)
                .month(12)
                .highestBalance(new BigDecimal("15000.00"))
                .lowestBalance(new BigDecimal("10000.00"))
                .averageBalance(new BigDecimal("12000.00"))
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 2)
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        CreditScoreMonthlyReport creditScoreMonthlyReportNovember2020 = CreditScoreMonthlyReport.builder()
                .id(UUID.randomUUID())
                .year(2020)
                .month(11)
                .highestBalance(new BigDecimal("10000.00"))
                .lowestBalance(new BigDecimal("5000.00"))
                .averageBalance(new BigDecimal("6000.00"))
                .categorizedAmount(Category.REVENUE, new BigDecimal("10000.00"), 1)
                .categorizedAmount(Category.OTHER_INCOME, new BigDecimal("1000.00"), 2)
                .categorizedAmount(Category.SALES_TAX, new BigDecimal("1000.00"), 3)
                .categorizedAmount(Category.OTHER_EXPENSES, new BigDecimal("5000.00"), 4)
                .incomingTransactionsSize(6)
                .outgoingTransactionsSize(4)
                .build();

        creditScoreMonthlyReportDecember2020.setCreditScoreReport(creditScoreReport);
        creditScoreMonthlyReportNovember2020.setCreditScoreReport(creditScoreReport);
        creditScoreReport.setCreditScoreMonthly(Set.of(creditScoreMonthlyReportDecember2020, creditScoreMonthlyReportNovember2020));
        creditScoreReport.setSignature("Kxf6gtjpDwubibfnqaTEYavr/yE9XAEY7FmtUCZLmQyIzlqKJS7GSV6nzOCbsZxsNzpOhTQIrmeDIq+9HwBb90qqy/6jHX3yy8WtO93vScjyC6/H95Taf2h1+lHWyEhEU+jRB4glg5PuTREToPcYRmVUDIyLtI7s6WF7SR1nHjL8t0eDRwVTRQE8O5ndzdwIKAgIUpdzitNsnSIfkGHKZwlABcB/bRdJ/tsnpIq+zDD6g0qFjD3x1esAPWvtzwGOEOf4hJc53GyHet1UE2gXQmdGzGti0mt5mRXQq8eVJn/hip2tHfYnDq6v1x0LnOcH3izSTIl7Nkvp9YCmpXjejytInEI/wN/G5s6UDstUUs0U0l3w7Ixa0jQVoVfYWBPt5e8Jn0ru3OPiYyQYA/+cVqU3OrpvCnpHSJaGlysaCn8SOTH6YFKV49xz+J2flHm2Xx+TwHTSCdmkR2SWfLyRjRmvPduhatv1p++4R9lD29Nc4mm0r4/dWmh7WNd5bGh2FHJiup3GcEcLx77Uc1mk5mOjN7CHbU4d78rFgf6YLCcjqh+N0FQAv67BPIlrTfbN6kP41BhBU4iSKkVlYwzBOOJAw45kfxPt3KBXnBRWAdXol91SF2RATw5YU7tmoypcMOxXZQBNRbwXLEJjUZd7bevDodpvaW8AjKKH1uMY7aI=");
        creditScoreReport.setSignatureKeyId(SOME_REPORT_SIGNATURE_KEY_ID);

        creditScoreReportRepository.save(creditScoreReport);
    }

}
