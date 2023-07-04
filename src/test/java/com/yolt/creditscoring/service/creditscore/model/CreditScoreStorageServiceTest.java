package com.yolt.creditscoring.service.creditscore.model;

import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.user.model.CreditScoreUser;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CreditScoreStorageServiceTest {

    @Mock
    private CreditScoreReportRepository creditScoreReportRepository;

    private CreditScoreStorageService creditScoreStorageService;

    @BeforeEach
    void setUp() {
        creditScoreStorageService = new CreditScoreStorageService(creditScoreReportRepository);
    }

    @Test
    void shouldSaveCreditScoreReportForGivenUser() {
        //Given
        ArgumentCaptor<CreditScoreReport> creditScoreReportArgumentCaptor = ArgumentCaptor.forClass(CreditScoreReport.class);

        CreditScoreUser user = new CreditScoreUser();
        user.setClientId(SOME_CLIENT_ID);

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

        ReportSignature reportSignature = ReportSignature.builder()
                .keyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .jsonPaths(List.of("$['userId']", "$['iban']", "$['initialBalance']"))
                .signature(SOME_REPORT_SIGNATURE)
                .build();

        //When
        creditScoreStorageService.saveCreditScoreReportForGivenUser(creditScoreReportDTO, reportSignature, SOME_USER_ID);

        //Then
        then(creditScoreReportRepository).should().save(creditScoreReportArgumentCaptor.capture());

        CreditScoreReport result = creditScoreReportArgumentCaptor.getValue();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreditScoreUserId()).isEqualTo(SOME_USER_ID);
        assertThat(result.getInitialBalance()).isEqualTo(new BigDecimal("5000.00"));
        assertThat(result.getNewestTransactionDate()).isEqualTo(LocalDate.of(2021, 1, 25));
        assertThat(result.getOldestTransactionDate()).isEqualTo(LocalDate.of(2020, 11, 2));
        assertThat(result.getAccountReference().getIban()).isEqualTo("NL79ABNA12345678901");
        assertThat(result.getCreditLimit()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(result.getTransactionsSize()).isEqualTo(10);
        assertThat(result.getSignature()).isEqualTo(SOME_REPORT_SIGNATURE.toString());
        assertThat(result.getSignatureKeyId()).isEqualTo(SOME_REPORT_SIGNATURE_KEY_ID);

        Assertions.assertThat(result.getCreditScoreMonthly()).extracting("year", "month", "highestBalance",
                        "lowestBalance")
                .containsOnly(
                        tuple(2021, 1, new BigDecimal("5750.00"), new BigDecimal("3750.00")));

        var months = result.getCreditScoreMonthly().toArray(new CreditScoreMonthlyReport[]{});
        assertThat(months[0].getCategorizedAmounts().values())
                .extracting("category", "amount")
                .containsOnly(
                        tuple(Category.OTHER_INCOME, new BigDecimal("2000.00")),
                        tuple(Category.OTHER_EXPENSES, new BigDecimal("-750.00"))
                );
    }
}
