package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionCycleDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CycleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

class TotalRecurringTransactionsAggregatorTest {

    private final TotalRecurringTransactionsAggregator totalRecurringTransactionsAggregator = new TotalRecurringTransactionsAggregator();

    @Test
    void shouldCorrectlyAggregateRecurringTransactions() {
        // Given
        List<CreditScoreTransactionDTO> transactions = List.of(
                createTransaction(1, 2021, UUID.fromString("60ae199c-bf67-428a-b709-9c6fe5a3bb5f")),
                createTransaction(1, 2021, UUID.fromString("d9e5be77-88b3-4c4f-87fb-6b4bc13ed84f")),
                createTransaction(1, 2021, UUID.fromString("287a614b-a466-4a08-8bf7-5743684ec1c9")),
                createTransaction(1, 2021, UUID.fromString("39caa26f-e2be-41a5-a4b6-041fb201f50d")),
                createTransaction(2, 2021, UUID.fromString("a6b5ef89-d4ed-4fbe-9938-76c6fdbdf375")),
                createTransaction(2, 2021, UUID.fromString("c4ddd20a-552b-4add-a84b-77182beb6647")),
                createTransaction(3, 2021, UUID.fromString("e75dba28-50d4-4139-bf5a-e931fbdbef80")),
                createTransaction(3, 2021, UUID.fromString("a18a585c-286b-4646-ab51-2c415e20cbf4")),
                createTransaction(4, 2021, UUID.fromString("919c8c04-1f36-4d50-992e-68992b1ddfd4")),
                createTransaction(4, 2021, UUID.fromString("adda817a-ff23-4eb7-a7c3-a8f4cefc96f4")),
                createTransaction(5, 2021, null),
                createTransaction(5, 2021, null),
                createTransaction(5, 2021, null),
                createTransaction(7, 2021, UUID.fromString("adb19257-e943-4636-bbbe-bc9388ac65a5")),
                createTransaction(7, 2021, null),
                createTransaction(7, 2021, UUID.fromString("44cba519-e196-478f-b176-d7cfabe7ac81"))
        );

        List<CreditScoreTransactionCycleDTO> cycleTransactions = List.of(
                createCycleTransaction(CycleType.CREDIT, "100.00", UUID.fromString("60ae199c-bf67-428a-b709-9c6fe5a3bb5f")),
                createCycleTransaction(CycleType.CREDIT, "300.00", UUID.fromString("d9e5be77-88b3-4c4f-87fb-6b4bc13ed84f")),
                createCycleTransaction(CycleType.DEBIT, "-50.00", UUID.fromString("287a614b-a466-4a08-8bf7-5743684ec1c9")),
                createCycleTransaction(CycleType.DEBIT, "-150.00", UUID.fromString("39caa26f-e2be-41a5-a4b6-041fb201f50d")),
                createCycleTransaction(CycleType.CREDIT, "100.00", UUID.fromString("a6b5ef89-d4ed-4fbe-9938-76c6fdbdf375")),
                createCycleTransaction(CycleType.DEBIT, "-100.00", UUID.fromString("c4ddd20a-552b-4add-a84b-77182beb6647")),
                createCycleTransaction(CycleType.CREDIT, "100.00", UUID.fromString("e75dba28-50d4-4139-bf5a-e931fbdbef80")),
                createCycleTransaction(CycleType.CREDIT, "100.00", UUID.fromString("a18a585c-286b-4646-ab51-2c415e20cbf4")),
                createCycleTransaction(CycleType.DEBIT, "-100.00", UUID.fromString("919c8c04-1f36-4d50-992e-68992b1ddfd4")),
                createCycleTransaction(CycleType.DEBIT, "-100.00", UUID.fromString("adda817a-ff23-4eb7-a7c3-a8f4cefc96f4")),
                createCycleTransaction(CycleType.CREDIT, "100.00", UUID.fromString("adb19257-e943-4636-bbbe-bc9388ac65a5")),
                createCycleTransaction(CycleType.DEBIT, "-100.00", UUID.fromString("44cba519-e196-478f-b176-d7cfabe7ac81"))
        );

        // When
        Set<MonthlyRecurringTransactionsDTO> results = totalRecurringTransactionsAggregator.calculateRecurringTransactions(transactions, cycleTransactions);

        // Then
        assertThat(results)
                .extracting("month", "year", "incomeRecurringAmount", "incomeRecurringSize", "outcomeRecurringAmount", "outcomeRecurringSize")
                .containsOnly(
                        tuple(1, 2021, new BigDecimal("400.00"), 2, new BigDecimal("200.00"), 2),
                        tuple(2, 2021, new BigDecimal("100.00"), 1, new BigDecimal("100.00"), 1),
                        tuple(3, 2021, new BigDecimal("200.00"), 2, new BigDecimal("0"), 0),
                        tuple(4, 2021, new BigDecimal("0"), 0, new BigDecimal("200.00"), 2),
                        tuple(7, 2021, new BigDecimal("100.00"), 1, new BigDecimal("100.00"), 1)
                );
    }

    @Test
    void shouldCorrectlyHandleCaseWhenThereAreNoMatchesForCyclesTransactions() {
        // Given
        List<CreditScoreTransactionDTO> transactions = List.of(
                createTransaction(1, 2021, UUID.fromString("60ae199c-bf67-428a-b709-9c6fe5a3bb5f")),
                createTransaction(1, 2021, UUID.fromString("d9e5be77-88b3-4c4f-87fb-6b4bc13ed84f")),
                createTransaction(1, 2021, UUID.fromString("287a614b-a466-4a08-8bf7-5743684ec1c9")),
                createTransaction(1, 2021, UUID.fromString("39caa26f-e2be-41a5-a4b6-041fb201f50d")),
                createTransaction(2, 2021, UUID.fromString("a6b5ef89-d4ed-4fbe-9938-76c6fdbdf375")),
                createTransaction(2, 2021, UUID.fromString("c4ddd20a-552b-4add-a84b-77182beb6647"))
        );

        List<CreditScoreTransactionCycleDTO> cycleTransactions = List.of();

        // When
        Set<MonthlyRecurringTransactionsDTO> results = totalRecurringTransactionsAggregator.calculateRecurringTransactions(transactions, cycleTransactions);

        // Then
        assertThat(results).isEmpty();
    }

    private CreditScoreTransactionDTO createTransaction(int month, int year, UUID cycleId) {
        return CreditScoreTransactionDTO.builder()
                .date(LocalDate.of(year, month, 1))
                .cycleId(cycleId)
                .build();
    }

    private CreditScoreTransactionCycleDTO createCycleTransaction(CycleType cycleType, String amount, UUID cycleId) {
        return CreditScoreTransactionCycleDTO.builder()
                .cycleId(cycleId)
                .cycleType(cycleType)
                .amount(new BigDecimal(amount))
                .build();
    }
}