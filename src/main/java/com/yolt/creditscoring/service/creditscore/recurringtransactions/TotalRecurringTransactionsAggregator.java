package com.yolt.creditscoring.service.creditscore.recurringtransactions;


import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionCycleDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CycleType;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Validated
public class TotalRecurringTransactionsAggregator {

    public Set<@Valid MonthlyRecurringTransactionsDTO> calculateRecurringTransactions(List<CreditScoreTransactionDTO> transactions,
                                                                                      List<CreditScoreTransactionCycleDTO> cycleTransactions) {

        List<PairedTransactionAndCycleTransactionDTO> pairedTransactionAndCycleTransactions =
                pairTransactionsWithCycleTransactions(transactions, cycleTransactions);

        Map<LocalDate, List<PairedTransactionAndCycleTransactionDTO>> transactionsGroupedByMonth =
                groupTransactionsPerMonth(pairedTransactionAndCycleTransactions);

        return transactionsGroupedByMonth.entrySet().stream()
                .map(entry -> calculateMonthlyTransactionCycles(entry.getKey(), entry.getValue()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static List<PairedTransactionAndCycleTransactionDTO> pairTransactionsWithCycleTransactions(List<CreditScoreTransactionDTO> transactions, List<CreditScoreTransactionCycleDTO> cycleTransactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getCycleId() != null)
                .flatMap(transaction -> cycleTransactions.stream()
                        .filter(cycleTransaction -> transaction.getCycleId().equals(cycleTransaction.getCycleId()))
                        .map(cycleTransaction -> new PairedTransactionAndCycleTransactionDTO(transaction, cycleTransaction))
                ).toList();
    }

    private static Map<LocalDate, List<PairedTransactionAndCycleTransactionDTO>> groupTransactionsPerMonth(List<PairedTransactionAndCycleTransactionDTO> pairedTransactionAndCycleTransactions) {
        return pairedTransactionAndCycleTransactions.stream()
                .collect(groupingBy(pairedTransactionAndCycleTransaction ->
                        pairedTransactionAndCycleTransaction.transaction().getDate().withDayOfMonth(1)));
    }

    private static MonthlyRecurringTransactionsDTO calculateMonthlyTransactionCycles(LocalDate date, List<PairedTransactionAndCycleTransactionDTO> transactionsWithCycleTransactions) {
        List<CreditScoreTransactionCycleDTO> incomingTransactionCycles = getTransactionCyclesByType(transactionsWithCycleTransactions, CycleType.CREDIT);
        List<CreditScoreTransactionCycleDTO> outgoingTransactionCycles = getTransactionCyclesByType(transactionsWithCycleTransactions, CycleType.DEBIT);

        return MonthlyRecurringTransactionsDTO.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .incomeRecurringAmount(getTotalAmountForCycle(incomingTransactionCycles))
                .incomeRecurringSize(incomingTransactionCycles.size())
                .outcomeRecurringAmount(getTotalAmountForCycle(outgoingTransactionCycles))
                .outcomeRecurringSize(outgoingTransactionCycles.size())
                .build();
    }

    private static List<CreditScoreTransactionCycleDTO> getTransactionCyclesByType(List<PairedTransactionAndCycleTransactionDTO> transactionsWithCycleTransactions,
                                                                            CycleType cycleType) {
        return transactionsWithCycleTransactions.stream()
                .map(PairedTransactionAndCycleTransactionDTO::cycleTransaction)
                .filter(transactionCycle ->
                        cycleType.equals(transactionCycle.getCycleType()))
                .toList();
    }

    private static BigDecimal getTotalAmountForCycle(List<CreditScoreTransactionCycleDTO> cycleTransactions) {
        return cycleTransactions.stream()
                .map(creditScoreTransactionCycleDTO -> creditScoreTransactionCycleDTO.getAmount().abs())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record PairedTransactionAndCycleTransactionDTO(
            CreditScoreTransactionDTO transaction,
            CreditScoreTransactionCycleDTO cycleTransaction) {
    }
}
