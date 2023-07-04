package com.yolt.creditscoring.usecase;

import com.yolt.creditscoring.service.audit.UserAuditService;
import com.yolt.creditscoring.service.client.ClientStorageService;
import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.creditscore.storage.CreditScoreStorageService;
import com.yolt.creditscoring.service.creditscore.storage.dto.response.user.UserReportDTO;
import com.yolt.creditscoring.service.user.CreditScoreUserDTO;
import com.yolt.creditscoring.service.user.UserStorageService;
import com.yolt.creditscoring.service.user.model.InvitationStatus;
import com.yolt.creditscoring.service.userjourney.UserJourneyService;
import com.yolt.creditscoring.service.yoltapi.YoltProvider;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.usecase.dto.CreditScoreUserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.yolt.creditscoring.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ConfirmCreditScoreReportUseCaseTest {

    @Mock
    private UserStorageService userStorageService;

    @Mock
    private CreditScoreStorageService creditScoreStorageService;

    @Mock
    private ClientStorageService clientService;

    @Mock
    private UserJourneyService userJourneyService;

    @Mock
    private UserAuditService userAuditService;

    @Mock
    private YoltProvider yoltProvider;

    private ConfirmCreditScoreReportUseCase confirmCreditScoreReportUseCase;

    @BeforeEach
    void setUp() {
        confirmCreditScoreReportUseCase = new ConfirmCreditScoreReportUseCase(
                userStorageService,
                creditScoreStorageService,
                clientService,
                userJourneyService,
                userAuditService,
                yoltProvider
        );
    }

    @Test
    void shouldCorrectlyReturnUserCreditScoreReport() {
        // Given
        given(userStorageService.getCreditScoreUserInvitationStatus(SOME_USER_ID))
                .willReturn(InvitationStatus.ACCOUNT_SELECTED);
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .selectedAccountId(SOME_YOLT_USER_ACCOUNT_ID)
                        .build());
        given(clientService.getClientAdditionalReportTextBasedOnClientId(SOME_CLIENT_ID))
                .willReturn(SOME_CLIENT_ADDITIONAL_TEXT);

        given(yoltProvider.getAccounts(SOME_YOLT_USER_ID))
                .willReturn(List.of(CreditScoreAccountDTO.builder()
                        .id(SOME_YOLT_USER_ACCOUNT_ID)
                        .balance(new BigDecimal("5000.00"))
                        .currency("EUR")
                        .creditLimit(new BigDecimal("1000.00"))
                        .lastDataFetchTime(SOME_FIXED_TEST_DATE)
                        .accountReference(AccountReference.builder()
                                .iban("NL79ABNA12345678901")
                                .build())
                        .build()));

        // When
        CreditScoreUserResponseDTO result = confirmCreditScoreReportUseCase.getReportForUser(SOME_USER_ID);

        // Then
        assertThat(result.getReport()).isEqualTo(
                UserReportDTO.builder()
                        .userId(SOME_USER_ID)
                        .initialBalance(new BigDecimal("5000.00"))
                        .currency("EUR")
                        .iban("NL79ABNA12345678901")
                        .creditLimit(new BigDecimal("1000.00"))
                        .lastDataFetchTime(SOME_FIXED_TEST_DATE)
                        .newestTransactionDate(SOME_FIXED_TEST_DATE.toLocalDate())
                        .oldestTransactionDate(SOME_FIXED_TEST_DATE.toLocalDate().minusMonths(18))
                        .build());
        assertThat(result.getAdditionalTextReport()).isEqualTo(SOME_CLIENT_ADDITIONAL_TEXT);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenReportIsNotGeneratedYet() {
        // Given
        given(userStorageService.findById(SOME_USER_ID))
                .willReturn(CreditScoreUserDTO.builder()
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .build());
        given(userStorageService.getCreditScoreUserInvitationStatus(SOME_USER_ID))
                .willReturn(InvitationStatus.ACCOUNT_SELECTED);
        given(clientService.getClientAdditionalReportTextBasedOnClientId(SOME_CLIENT_ID))
                .willReturn(SOME_CLIENT_ADDITIONAL_TEXT);
        given(yoltProvider.getAccounts(SOME_YOLT_USER_ID))
                .willThrow(new RuntimeException("Error when fetching accounts"));
        // When
        Throwable thrown = catchThrowable(() -> confirmCreditScoreReportUseCase.getReportForUser(SOME_USER_ID));

        // Then
        assertThat(thrown).isInstanceOf(RuntimeException.class)
                .hasMessage("Error when fetching accounts");
    }

    @Test
    void shouldThrowIllegalStateExceptionWhenThereIsErrorStatusForUser() {
        // Given
        given(userStorageService.findById(SOME_USER_ID)).willReturn(CreditScoreUserDTO.builder().clientId(SOME_CLIENT_ID).build());
        given(userStorageService.getCreditScoreUserInvitationStatus(SOME_USER_ID)).willReturn(InvitationStatus.CALCULATION_ERROR);

        // When
        Throwable thrown = catchThrowable(() -> confirmCreditScoreReportUseCase.getReportForUser(SOME_USER_ID));

        // Then
        assertThat(thrown).isInstanceOf(IllegalStateException.class)
                .hasMessage("Wrong user workflow state");
    }

    @Test
    void shouldUserBeAbleToRefuseToShareReport() {
        // Given
        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .build());

        given(creditScoreStorageService.findCreditScoreReportIdByUserId(SOME_USER_ID)).willReturn(
                Optional.empty());

        // When
        confirmCreditScoreReportUseCase.refuseReportShare(SOME_USER_ID);

        // Then
        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);
        then(userStorageService).should().refuse(SOME_USER_ID);
        then(userStorageService).should().removeYoltUser(SOME_USER_ID);
        then(userJourneyService).should().registerReportRefused(SOME_CLIENT_ID, SOME_USER_ID);
    }

    /**
     * Situation that report is calculated but user refuse to share it should not happen.
     * During DB migration or refactoring is possible to introduce error and this test is to handle it.
     */
    @Test
    void shouldUserBeAbleToRefuseToShareReportWhenReportWasCalculated() {
        // Given
        given(userStorageService.findById(SOME_USER_ID)).willReturn(
                CreditScoreUserDTO.builder()
                        .id(SOME_USER_ID)
                        .clientId(SOME_CLIENT_ID)
                        .yoltUserId(SOME_YOLT_USER_ID)
                        .build());

        given(creditScoreStorageService.findCreditScoreReportIdByUserId(SOME_USER_ID)).willReturn(
                Optional.of(SOME_CREDIT_REPORT_ID));

        // When
        confirmCreditScoreReportUseCase.refuseReportShare(SOME_USER_ID);

        // Then
        then(yoltProvider).should().removeUser(SOME_YOLT_USER_ID);
        then(userStorageService).should().refuse(SOME_USER_ID);
        then(userStorageService).should().removeYoltUser(SOME_USER_ID);
        then(userJourneyService).should().registerReportRefused(SOME_CLIENT_ID, SOME_USER_ID);
    }

    @Test
    void shouldReturnRedirectUrlForSharedReport() {
        // Given
        given(clientService.getClientRedirectUrl(SOME_CLIENT_ID)).willReturn(SOME_CLIENT_REDIRECT_URL);

        // When
        String result = confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(SOME_USER_ID, SOME_CLIENT_ID, true);

        // Then
        assertThat(result).isEqualTo(SOME_CLIENT_REDIRECT_URL + "?userId=" + SOME_USER_ID + "&status=confirm");
    }

    @Test
    void shouldReturnRedirectUrlForRefusedReport() {
        // Given
        given(clientService.getClientRedirectUrl(SOME_CLIENT_ID)).willReturn(SOME_CLIENT_REDIRECT_URL);

        // When
        String result = confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(SOME_USER_ID, SOME_CLIENT_ID, false);

        // Then
        assertThat(result).isEqualTo(SOME_CLIENT_REDIRECT_URL + "?userId=" + SOME_USER_ID + "&status=refuse");
    }

    @Test
    void shouldReturnEmptyRedirectUrlWhenRedirectUrlIsEmptyForClient() {
        // Given
        given(clientService.getClientRedirectUrl(SOME_CLIENT_ID)).willReturn("");

        // When
        String result = confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(SOME_USER_ID, SOME_CLIENT_ID, true);

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyRedirectUrlWhenRedirectUrlIsNullForClient() {
        // Given
        given(clientService.getClientRedirectUrl(SOME_CLIENT_ID)).willReturn(null);

        // When
        String result = confirmCreditScoreReportUseCase.getClientRedirectUrlIfPresent(SOME_USER_ID, SOME_CLIENT_ID, true);

        // Then
        assertThat(result).isEqualTo("");
    }
}
