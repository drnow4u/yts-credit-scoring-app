package com.yolt.creditscoring.service.estimate.provider;

import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimateProbabilityOfDefaultDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.ProbabilityOfDefaultStorage;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;
import com.yolt.creditscoring.service.estimate.provider.exception.EstimateAPIException;
import com.yolt.creditscoring.service.estimate.provider.exception.NotEnoughTransactionDataException;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class EstimateProviderTest {

    @Mock
    private EstimateHttpClient httpClient;

    @InjectMocks
    private EstimateProvider estimateProvider;

    @Test
    void shouldReturnPDScoreWhenDataWillBeSuccessfullyFetch() throws NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        CreditScoreAccountDTO account = CreditScoreAccountDTO.builder()
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .balance(BigDecimal.valueOf(123.45))
                .transactions(List.of(CreditScoreTransactionDTO.builder()
                        .date(LocalDate.parse("2021-08-01"))
                        .amount(BigDecimal.valueOf(45.67))
                        .build()))
                .build();
        given(httpClient.getPDScoreForGivenAccount(any()))
                .willReturn(EstimateProbabilityOfDefaultDTO.builder()
                        .score(12)
                        .grade(RiskClassification.G)
                        .build());

        // When
        ProbabilityOfDefaultStorage result = estimateProvider.calculatePDForReport(account);

        // Then
        assertThat(result.getScore()).isEqualTo(12);
        assertThat(result.getGrade()).isEqualTo(RiskClassification.G);
        assertThat(result.getStatus()).isEqualTo(PdStatus.COMPLETED);
    }

    @Test
    void shouldReturnErrorInThePDScoreIfForSomeReasonFetchWillFail() throws NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        CreditScoreAccountDTO account = CreditScoreAccountDTO.builder()
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .balance(BigDecimal.valueOf(123.45))
                .transactions(List.of(CreditScoreTransactionDTO.builder()
                        .date(LocalDate.parse("2021-08-01"))
                        .amount(BigDecimal.valueOf(45.67))
                        .build()))
                .build();
        given(httpClient.getPDScoreForGivenAccount(any())).willThrow(new EstimateAPIException("Something went wrong..."));

        // When
        ProbabilityOfDefaultStorage result = estimateProvider.calculatePDForReport(account);

        // Then
        assertThat(result.getScore()).isNull();
        assertThat(result.getGrade()).isNull();
        assertThat(result.getStatus()).isEqualTo(PdStatus.ERROR);
    }

    @Test
    void shouldReturnNotEnoughTransactionErrorInThePDScore() throws NoSuchAlgorithmException, KeyStoreException, IOException {
        // Given
        CreditScoreAccountDTO account = CreditScoreAccountDTO.builder()
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .balance(BigDecimal.valueOf(123.45))
                .transactions(List.of(CreditScoreTransactionDTO.builder()
                        .date(LocalDate.parse("2021-08-01"))
                        .amount(BigDecimal.valueOf(45.67))
                        .build()))
                .build();
        given(httpClient.getPDScoreForGivenAccount(any())).willThrow(new NotEnoughTransactionDataException("Not enough transactions. Please provide six full months of transactions."));

        // When
        ProbabilityOfDefaultStorage result = estimateProvider.calculatePDForReport(account);

        // Then
        assertThat(result.getScore()).isNull();
        assertThat(result.getGrade()).isNull();
        assertThat(result.getStatus()).isEqualTo(PdStatus.ERROR_NOT_ENOUGH_TRANSACTIONS);
    }
}
