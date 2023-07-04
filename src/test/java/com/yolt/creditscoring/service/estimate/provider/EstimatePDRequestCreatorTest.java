package com.yolt.creditscoring.service.estimate.provider;

import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimatePDRequestDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class EstimatePDRequestCreatorTest {

    @Test
    void shouldCorrectlyCreateEstimatePDRequestDTO() {
        // Given
        CreditScoreAccountDTO account = CreditScoreAccountDTO.builder()
                .balance(new BigDecimal("1000.00"))
                .accountReference(AccountReference.builder().iban("NL79ABNA12345678901").build())
                .transactions(Arrays.asList(
                        CreditScoreTransactionDTO.builder()
                                .currency("EUR")
                                .amount(new BigDecimal("-100.30"))
                                .date(LocalDate.of(2020, 12, 31))
                                .build(),
                        CreditScoreTransactionDTO.builder()
                                .currency("EUR")
                                .amount(new BigDecimal("-100.30"))
                                .date(LocalDate.of(2021, 1, 1))
                                .build(),
                        CreditScoreTransactionDTO.builder()
                                .currency("EUR")
                                .amount(new BigDecimal("200.1"))
                                .date(LocalDate.of(2021, 3, 2))
                                .build(),
                        CreditScoreTransactionDTO.builder()
                                .currency("EUR")
                                .amount(new BigDecimal("-0.30"))
                                .date(LocalDate.of(2021, 6, 30))
                                .build(),
                        CreditScoreTransactionDTO.builder()
                                .currency("EUR")
                                .amount(new BigDecimal("-0.30"))
                                .date(LocalDate.of(2021, 7, 3))
                                .build())
                )
                .build();

        // When
        EstimatePDRequestDTO result = EstimatePDRequestCreator.createRequest(account);

        // Then
        assertThat(result.getReferenceId()).isNotNull();
        assertThat(result.getCurrentBalance().getScale()).isEqualTo(2);
        assertThat(result.getCurrentBalance().getUnscaledValue()).isEqualTo(new BigInteger("100000"));

        assertThat(result.getTransactions().get(0).getId()).isNotNull();
        assertThat(result.getTransactions().get(0).getCurrencyCode()).isEqualTo("EUR");
        assertThat(result.getTransactions().get(0).getDateBooked()).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(result.getTransactions().get(0).getAmount().getScale()).isEqualTo(2);
        assertThat(result.getTransactions().get(0).getAmount().getUnscaledValue()).isEqualTo(new BigInteger("-10030"));

        assertThat(result.getTransactions().get(1).getId()).isNotNull();
        assertThat(result.getTransactions().get(1).getCurrencyCode()).isEqualTo("EUR");
        assertThat(result.getTransactions().get(1).getDateBooked()).isEqualTo(LocalDate.of(2021, 3, 2));
        assertThat(result.getTransactions().get(1).getAmount().getScale()).isEqualTo(1);
        assertThat(result.getTransactions().get(1).getAmount().getUnscaledValue()).isEqualTo(new BigInteger("2001"));

        assertThat(result.getTransactions().get(2).getId()).isNotNull();
        assertThat(result.getTransactions().get(2).getCurrencyCode()).isEqualTo("EUR");
        assertThat(result.getTransactions().get(2).getDateBooked()).isEqualTo(LocalDate.of(2021, 6, 30));
        assertThat(result.getTransactions().get(2).getAmount().getScale()).isEqualTo(2);
        assertThat(result.getTransactions().get(2).getAmount().getUnscaledValue()).isEqualTo(new BigInteger("-30"));

    }
}