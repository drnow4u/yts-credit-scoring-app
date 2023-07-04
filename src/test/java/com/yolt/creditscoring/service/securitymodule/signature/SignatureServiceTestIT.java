package com.yolt.creditscoring.service.securitymodule.signature;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
@TestPropertySource(properties = {
        "yolt.creditScoreExecutor.async=false",
})
class SignatureServiceTestIT {

    @Autowired
    private SignatureService signatureService;

    @Test
    void checkSignature() {
        // Given
        ReportSaveDTO creditScoreReportDTO = ReportSaveDTO.builder()
                .initialBalance(new BigDecimal("5000.00"))
                .newestTransactionDate(LocalDate.of(2021, 1, 25))
                .oldestTransactionDate(LocalDate.of(2020, 11, 2))
                .iban("NL79ABNA12345678901")
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .creditScoreMonthly(Set.of(MonthlyReportSaveDTO.builder()
                        .year(2021)
                        .month(1)
                        .highestBalance(new BigDecimal("5750.00"))
                        .lowestBalance(new BigDecimal("3750.00"))
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("2000.00"))
                                .category(Category.OTHER_INCOME)
                                .build())
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("-750.00"))
                                .category(Category.OTHER_EXPENSES)
                                .build())
                        .build()))
                .build();
        // When
        var signature = signatureService.sign(creditScoreReportDTO);

        // Then
        assertThat(signature.getSignature().toString()).hasSize(344);
        assertThat(signature.getJsonPaths()).containsExactly(
                "$['iban']",
                "$['initialBalance']",
                "$['newestTransactionDate']",
                "$['oldestTransactionDate']",
                "$['creditLimit']",
                "$['transactionsSize']");
        assertThat(signatureService.verify(
                creditScoreReportDTO,
                ReportSignature.builder()
                        .signature(signature.getSignature())
                        .keyId(signature.getKeyId())
                        .jsonPaths(signature.getJsonPaths())
                        .build())
        ).isTrue();
    }

    /*
     * This scenario is detected that user swap filed e.g. inbound and outbound amount in credit report.
     * Signature is not to protect consistency of data in DB.
     * Requested by Yolt-Security https://yolt.atlassian.net/browse/YTSAPP-537
     */
    @Test
    @Disabled // TODO: not applicable at this moment
    void shouldNotAbuseForSwappingFields() {
        // Given
        ReportSaveDTO creditScoreReport = ReportSaveDTO.builder()
                .initialBalance(new BigDecimal("5000.00"))
                .newestTransactionDate(LocalDate.of(2021, 1, 25))
                .oldestTransactionDate(LocalDate.of(2020, 11, 2))
                .iban("NL79ABNA12345678901")
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .creditScoreMonthly(Set.of(MonthlyReportSaveDTO.builder()
                        .year(2021)
                        .month(1)
                        .highestBalance(new BigDecimal("5750.00"))
                        .lowestBalance(new BigDecimal("3750.00"))
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("100.00"))
                                .category(Category.OTHER_INCOME)
                                .build())
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("-2000.00"))
                                .category(Category.OTHER_EXPENSES)
                                .build())
                        .build()))
                .build();
        var signature = signatureService.sign(creditScoreReport);

        // When
        ReportSaveDTO creditScoreReportSwappedFields = ReportSaveDTO.builder()
                .initialBalance(new BigDecimal("5000.00"))
                .newestTransactionDate(LocalDate.of(2021, 1, 25))
                .oldestTransactionDate(LocalDate.of(2020, 11, 2))
                .iban("NL79ABNA12345678901")
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .creditScoreMonthly(Set.of(MonthlyReportSaveDTO.builder()
                        .year(2021)
                        .month(1)
                        .highestBalance(new BigDecimal("5750.00"))
                        .lowestBalance(new BigDecimal("3750.00"))
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("2000.00"))
                                .category(Category.OTHER_INCOME)
                                .build())
                        .categoriesAmount(MonthlyCategoryReportSaveDTO.builder()
                                .amount(new BigDecimal("-100.00"))
                                .category(Category.OTHER_EXPENSES)
                                .build())
                        .build()))
                .build();
        var isValid = signatureService.verify(creditScoreReportSwappedFields, ReportSignature.builder()
                .signature(signature.getSignature())
                .keyId(signature.getKeyId())
                .jsonPaths(signature.getJsonPaths())
                .build());

        // Then
        assertThat(isValid).isFalse();
    }
}
