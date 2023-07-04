package com.yolt.creditscoring.utility.download;

import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryType;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminOverviewResponseDTO;
import com.yolt.creditscoring.usecase.dto.RiskClassificationDTO;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DownloadReportMapperTest {

    @Test
    void generateCsvForMonths() {
        // Given
        final Set<MonthlyAdminReportDTO> months = generateMonths();

        // When
        final byte[] bytes = DownloadReportMapper.generateCsvForMonths("EUR", months);

        String[] csvLines = new String(bytes).split("\n");

        // Then
        assertThat(csvLines[0]).isEqualTo("Year;Month;Currency;Highest balance;Lowest balance;Total incoming;# incoming transactions;Total outgoing;# outgoing transactions");
        assertThat(csvLines[1]).isEqualTo("2022;2;EUR;13000;345;60000;456;40000;456");
        assertThat(csvLines[2]).isEqualTo("2022;1;EUR;12000;123;50000;234;30000;234");
    }

    @Test
    void generateCsvForCategories() {
        // Given
        final List<SMECategoryDTO> categories = generateCategories();

        // When
        final byte[] bytes = DownloadReportMapper.generateCsvForCategories(categories);

        String[] csvLines = new String(bytes).split("\n");

        // Then
        assertThat(csvLines[0]).isEqualTo("Category name;Type;# total transactions;Average transaction amount;Total transaction amount");
        assertThat(csvLines[1]).isEqualTo("Salaries;INCOMING;123;1234;12345");
        assertThat(csvLines[2]).isEqualTo("Loans;OUTGOING;23;234;2345");
    }

    @Test
    void generateJsonForOverview() throws JSONException {
        // Given
        final CreditScoreAdminOverviewResponseDTO overview = generateOverview();
        String expectedJson = """
                {
                  "averageRecurringIncome": "2345",
                  "averageRecurringCosts": "234",
                  "startDate": [
                    2022,
                    1,
                    1
                  ],
                  "endDate": [
                    2022,
                    12,
                    31
                  ],
                  "incomingTransactionsSize": 12,
                  "outgoingTransactionsSize": 45,
                  "monthlyAverageIncome": "3456",
                  "monthlyAverageCost": "345",
                  "totalIncomeAmount": "123456",
                  "totalOutgoingAmount": "23456",
                  "averageIncomeTransactionAmount": "1234",
                  "averageOutcomeTransactionAmount": "123",
                  "vatTotalPayments": 23,
                  "vatAverage": "0.23",
                  "totalCorporateTax": "0.1",
                  "totalTaxReturns": "0.01"
                }
                """;

        // When
        final byte[] bytes = DownloadReportMapper.generateJson(overview);

        String json = new String(bytes);

        // Then
        JSONAssert.assertEquals(expectedJson, json, true);
    }

    @Test
    void generateJsonForEstimateReport() {
        // Given
        final RiskClassificationDTO estimateReport = generateEstimateReport();
        String expectedJson = """
                {"rateLower":0.2,"rateUpper":0.4,"grade":"F","status":"COMPLETED"}""";

        // When
        final byte[] bytes = DownloadReportMapper.generateJson(estimateReport);

        String json = new String(bytes);

        // Then
        assertThat(json).isEqualTo(expectedJson);
    }

    private static Set<MonthlyAdminReportDTO> generateMonths() {
        Set<MonthlyAdminReportDTO> months = new HashSet<>();
        MonthlyAdminReportDTO month1 = MonthlyAdminReportDTO.builder()
                .year(2022)
                .month(1)
                .highestBalance(BigDecimal.valueOf(12000))
                .lowestBalance(BigDecimal.valueOf(123))
                .totalIncoming(BigDecimal.valueOf(50000))
                .totalOutgoing(BigDecimal.valueOf(30000))
                .incomingTransactionsSize(234)
                .outgoingTransactionsSize(234)
                .build();
        months.add(month1);
        MonthlyAdminReportDTO month2 = MonthlyAdminReportDTO.builder()
                .year(2022)
                .month(2)
                .highestBalance(BigDecimal.valueOf(13000))
                .lowestBalance(BigDecimal.valueOf(345))
                .totalIncoming(BigDecimal.valueOf(60000))
                .totalOutgoing(BigDecimal.valueOf(40000))
                .incomingTransactionsSize(456)
                .outgoingTransactionsSize(456)
                .build();
        months.add(month2);

        return months;
    }

    private static List<SMECategoryDTO> generateCategories() {
        List<SMECategoryDTO> categories = new ArrayList<>();

        SMECategoryDTO category1 = SMECategoryDTO.builder()
                .categoryName(Category.SALARIES)
                .categoryType(SMECategoryType.INCOMING)
                .totalTransactions(123)
                .averageTransactionAmount(BigDecimal.valueOf(1234))
                .totalTransactionAmount(BigDecimal.valueOf(12345))
                .build();
        categories.add(category1);
        SMECategoryDTO category2 = SMECategoryDTO.builder()
                .categoryName(Category.LOANS)
                .categoryType(SMECategoryType.OUTGOING)
                .totalTransactions(23)
                .averageTransactionAmount(BigDecimal.valueOf(234))
                .totalTransactionAmount(BigDecimal.valueOf(2345))
                .build();
        categories.add(category2);
        return categories;
    }

    private static CreditScoreAdminOverviewResponseDTO generateOverview() {
        return CreditScoreAdminOverviewResponseDTO.builder()
                .endDate(LocalDate.of(2022, 12, 31))
                .startDate(LocalDate.of(2022, 1, 1))
                .averageIncomeTransactionAmount(BigDecimal.valueOf(1234))
                .averageOutcomeTransactionAmount(BigDecimal.valueOf(123))
                .averageRecurringCosts(BigDecimal.valueOf(234))
                .averageRecurringIncome(BigDecimal.valueOf(2345))
                .incomingTransactionsSize(12)
                .monthlyAverageCost(BigDecimal.valueOf(345))
                .monthlyAverageIncome(BigDecimal.valueOf(3456))
                .outgoingTransactionsSize(45)
                .totalCorporateTax(BigDecimal.valueOf(0.1))
                .totalIncomeAmount(BigDecimal.valueOf(123456))
                .totalOutgoingAmount(BigDecimal.valueOf(23456))
                .vatAverage(BigDecimal.valueOf(0.23))
                .vatTotalPayments(23)
                .totalTaxReturns(BigDecimal.valueOf(0.01))
                .build();
    }

    private static RiskClassificationDTO generateEstimateReport() {
        return new RiskClassificationDTO(0.2, 0.4, RiskClassification.F, PdStatus.COMPLETED);
    }
}