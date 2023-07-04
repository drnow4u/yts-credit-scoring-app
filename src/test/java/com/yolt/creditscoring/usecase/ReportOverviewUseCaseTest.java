package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.service.creditscore.category.SMECategoryDTO;
import com.yolt.creditscoring.service.creditscore.category.SMECategoryType;
import com.yolt.creditscoring.service.creditscore.indicators.TaxYearIndicatorsDTO;
import com.yolt.creditscoring.service.creditscore.model.Category;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ReportOverviewUseCaseTest {

    @Test
    void shouldReturnZeroValuesForEmptyCategoriesList() {
        // Given
        Map<Category, SMECategoryDTO> categories = Map.of();

        // When
        TaxYearIndicatorsDTO result = ReportOverviewUseCase.calculateTaxYearIndicatorsReport(categories);

        // Then
        assertThat(result.getVatTotalPayments()).isEqualTo(0);
        assertThat(result.getVatAverage()).isEqualTo("0");
        assertThat(result.getTotalCorporateTax()).isEqualTo("0");
    }

    @Test
    void shouldReturnZeroValuesForMissingSalesAndCorporateCategories() {
        // Given
        Map<Category, SMECategoryDTO> categories = Map.of(Category.OTHER_EXPENSES, SMECategoryDTO.builder()
                .categoryName(Category.OTHER_EXPENSES)
                .categoryType(SMECategoryType.OUTGOING)
                .totalTransactions(2)
                .totalTransactionAmount(new BigDecimal("1000.00"))
                .averageTransactionAmount(new BigDecimal("200.00"))
                .build());

        // When
        TaxYearIndicatorsDTO result = ReportOverviewUseCase.calculateTaxYearIndicatorsReport(categories);

        // Then
        assertThat(result.getVatTotalPayments()).isEqualTo(0);
        assertThat(result.getVatAverage()).isEqualTo("0");
        assertThat(result.getTotalCorporateTax()).isEqualTo("0");
    }

    @Test
    void shouldReturnValuesForSalesAndCorporateCategories() {
        // Given
        Map<Category, SMECategoryDTO> categories = Map.of(
                Category.OTHER_EXPENSES, SMECategoryDTO.builder()
                        .categoryName(Category.OTHER_EXPENSES)
                        .categoryType(SMECategoryType.OUTGOING)
                        .totalTransactions(5)
                        .totalTransactionAmount(new BigDecimal("1000.00"))
                        .averageTransactionAmount(new BigDecimal("200.00"))
                        .build(),
                Category.CORPORATE_INCOME_TAX, SMECategoryDTO.builder()
                        .categoryName(Category.CORPORATE_INCOME_TAX)
                        .categoryType(SMECategoryType.OUTGOING)
                        .totalTransactions(20)
                        .totalTransactionAmount(new BigDecimal("4000.00"))
                        .averageTransactionAmount(new BigDecimal("400.00"))
                        .build(),
                Category.SALES_TAX, SMECategoryDTO.builder()
                        .categoryName(Category.SALES_TAX)
                        .categoryType(SMECategoryType.OUTGOING)
                        .totalTransactions(50)
                        .totalTransactionAmount(new BigDecimal("10000.00"))
                        .averageTransactionAmount(new BigDecimal("2000.00"))
                        .build());

        // When
        TaxYearIndicatorsDTO result = ReportOverviewUseCase.calculateTaxYearIndicatorsReport(categories);

        // Then
        assertThat(result.getVatTotalPayments()).isEqualTo(50);
        assertThat(result.getVatAverage()).isEqualTo("2000.00");
        assertThat(result.getTotalCorporateTax()).isEqualTo("4000.00");
    }
}