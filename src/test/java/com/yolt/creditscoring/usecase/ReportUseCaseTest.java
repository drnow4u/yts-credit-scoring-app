package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.common.signature.SignatureCreditScoreReport;
import com.yolt.creditscoring.exception.UserNotFoundException;
import com.yolt.creditscoring.service.audit.AdminAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.InvalidSignatureDTO;
import com.yolt.creditscoring.service.securitymodule.semaevent.SemaEventService;
import com.yolt.creditscoring.service.securitymodule.signature.ReportSignature;
import com.yolt.creditscoring.service.securitymodule.signature.SignatureService;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.usecase.dto.CreditScoreAdminResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReportUseCaseTest {

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private CreditScoreStorageService creditScoreReportService;

    @Mock
    private ClientStorageService clientService;

    @Mock
    private SignatureService signatureService;

    @Mock
    private SemaEventService semaEventService;

    @Mock
    private AdminAuditService userAuditService;

    private ReportUseCase creditScoreReportCategorisedUseCase;

    @BeforeEach
    void setUp() {
        creditScoreReportCategorisedUseCase = new ReportUseCase(
                userStorageService,
                creditScoreReportService,
                clientService,
                signatureService,
                semaEventService,
                userAuditService
        );
    }

    @Test
    void shouldCorrectlyReturnAdminCreditScoreReport() {
        // Given
        BankAccountDetailsDTO creditScoreReport = BankAccountDetailsDTO.builder()
                .lastDataFetchTime(OffsetDateTime.parse("2020-10-09T01:02:03Z"))
                .userId(SOME_USER_ID)
                .initialBalance(new BigDecimal("5000.00"))
                .currency("EUR")
                .newestTransactionDate(LocalDate.of(2020, 12, 31))
                .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                .creditLimit(new BigDecimal("1000.00"))
                .transactionsSize(10)
                .iban("NL79ABNA12345678901")
                .build();

        ReportSignature reportSignature = ReportSignature.builder()
                .keyId(SOME_REPORT_SIGNATURE_KEY_ID)
                .jsonPaths(List.of("$['userId']", "$['iban']", "$['initialBalance']"))
                .signature(SOME_REPORT_SIGNATURE)
                .build();

        given(creditScoreReportService.getCreditScoreReportBankAccountDetails(SOME_USER_ID))
                .willReturn(Optional.of(creditScoreReport));
        given(creditScoreReportService.getReportSignature(any()))
                .willReturn(reportSignature);
        given(userStorageService.findById(SOME_USER_ID)).willReturn(CreditScoreUserDTO.builder().clientId(SOME_CLIENT_ID).status(InvitationStatus.COMPLETED).build());
        given(signatureService.verify(any(SignatureCreditScoreReport.class), eq(ReportSignature.builder()
                .signature(SOME_REPORT_SIGNATURE)
                .keyId(reportSignature.getKeyId())
                .jsonPaths(List.of("$['userId']", "$['iban']", "$['initialBalance']"))
                .build()))).willReturn(true);
        given(signatureService.getPublicKeyModulus(SOME_REPORT_SIGNATURE_KEY_ID)).willReturn(mock(PublicKey.class));

        // When
        CreditScoreAdminResponseDTO result = creditScoreReportCategorisedUseCase.getUserCreditScore(SOME_USER_ID, SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL);

        // Then
        assertThat(result.getAdminReport()).isEqualTo(
                BankAccountDetailsDTO.builder()
                        .lastDataFetchTime(OffsetDateTime.parse("2020-10-09T01:02:03Z"))
                        .userId(SOME_USER_ID)
                        .initialBalance(new BigDecimal("5000.00"))
                        .oldestTransactionDate(LocalDate.of(2020, 12, 1))
                        .newestTransactionDate(LocalDate.of(2020, 12, 31))
                        .currency("EUR")
                        .iban("NL79ABNA12345678901")
                        .creditLimit(new BigDecimal("1000.00"))
                        .transactionsSize(10)
                        .build());
        assertThat(result.getSignature()).isEqualTo(SOME_REPORT_SIGNATURE);
        then(semaEventService).should(never()).logIncorrectSignature(any(InvalidSignatureDTO.class), eq(SOME_USER_ID), eq(SOME_CLIENT_ID));
    }

    @Test
    void shouldReturnUserNotFoundIfAdminClientIdNotMatchUserClientId() {
        // Given
        UUID someDifferentClientId = UUID.randomUUID();
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder().clientId(someDifferentClientId).build());

        // When
        Throwable thrown = catchThrowable(() -> creditScoreReportCategorisedUseCase.getUserCreditScore(SOME_USER_ID, SOME_CLIENT_ID, SOME_CLIENT_ADMIN_ID, SOME_CLIENT_ADMIN_EMAIL));

        // Then
        assertThat(thrown).isInstanceOf(UserNotFoundException.class);
    }
}
