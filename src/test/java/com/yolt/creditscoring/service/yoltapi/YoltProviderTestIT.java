package com.yolt.creditscoring.service.yoltapi;

import com.yolt.creditscoring.IntegrationTest;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@IntegrationTest
class YoltProviderTestIT {

    @Autowired
    private YoltProvider yoltProvider;

    @Test
    void shouldFetchUserAccount() {
        // Given
        UUID yoltUserId = UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08");
        UUID accountId = UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1");

        // When
        CreditScoreAccountDTO result = yoltProvider.getAccountForCreditScoreCalculations(yoltUserId, accountId);

        // Then
        assertThat(result.getId()).isEqualTo(accountId);
        assertThat(result.getBalance()).isEqualTo(new BigDecimal("652.29"));
        assertThat(result.getCurrency()).isEqualTo("EUR");
        assertThat(result.getStatus()).isEqualTo("ENABLED");
        assertThat(result.getAccountReference().getIban()).isEqualTo("NL05INGB1234567890");
        assertThat(result.getUsage()).isEqualTo("PRIVATE");
        assertThat(result.getTransactions()).hasSize(44);
        assertThat(result.getCreditLimit()).isEqualTo("-5000.05");
    }

    @Test
    void shouldFetchOnePageTransaction() {
        // Given
        UUID yoltUserIdWithOnePageTransaction = UUID.fromString("497f6eca-6276-4993-bfeb-53cbbbba6f08");
        UUID accountIdWithOnePageTransaction = UUID.fromString("109d740d-2932-4916-a62d-22e363e34dc1");

        // When
        List<CreditScoreTransactionDTO> result =
                yoltProvider.getTransactions(yoltUserIdWithOnePageTransaction, accountIdWithOnePageTransaction);

        // Then
        assertThat(result).hasSize(44);
    }

    @Test
    void shouldFetchMultiplePageTransactions() {
        // Given
        UUID yoltUserIdWithMultiplePageTransactions = UUID.fromString("ab9b1013-2cbd-4281-a2b6-fbba0695bc54");
        UUID accountIdWithMultiplePageTransactions = UUID.fromString("fca606a4-c91a-4a40-a042-947d61ca1fb4");

        // When
        List<CreditScoreTransactionDTO> result =
                yoltProvider.getTransactions(yoltUserIdWithMultiplePageTransactions, accountIdWithMultiplePageTransactions);

        // Then
        assertThat(result).hasSize(44);
    }
}
