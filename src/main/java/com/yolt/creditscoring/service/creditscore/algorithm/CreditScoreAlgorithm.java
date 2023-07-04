package com.yolt.creditscoring.service.creditscore.algorithm;

import com.yolt.creditscoring.service.creditscore.model.Category;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyCategoryReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.MonthlyReportSaveDTO;
import com.yolt.creditscoring.service.creditscore.storage.dto.save.ReportSaveDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
public class CreditScoreAlgorithm {

    public ReportSaveDTO calculateCreditReport(CreditScoreAccountDTO account) {
        Objects.requireNonNull(account.getAccountReference(), "Account reference should not be null");
        List<CreditScoreTransactionDTO> transactions = account.getTransactions();
        if (transactions.isEmpty()) {
            return ReportSaveDTO.builder()
                    .iban(account.getAccountReference().getIban())
                    .bban(account.getAccountReference().getBban())
                    .sortCodeAccountNumber(account.getAccountReference().getSortCodeAccountNumber())
                    .maskedPan(account.getAccountReference().getMaskedPan())
                    .initialBalance(account.getBalance())
                    .lastDataFetchTime(account.getLastDataFetchTime())
                    .currency(account.getCurrency())
                    .creditLimit(account.getCreditLimit())
                    .transactionsSize(transactions.size())
                    .accountHolder(account.getAccountHolder())
                    .build();
        }

        Map<LocalDate, List<CreditScoreTransactionDTO>> transactionsGroupedByMonth = groupTransactionsPerMonth(transactions);

        List<BalanceHistory> balanceHistoryForTransactions =
                BalanceHistoryUtil.getBalanceHistoryForTransactions(account.getBalance(), transactions);

        Set<MonthlyReportSaveDTO> creditScoreMonthlyReports = transactionsGroupedByMonth.entrySet().stream()
                .map(entry -> calculateReportForGivenMonth(entry.getKey(), entry.getValue(), balanceHistoryForTransactions))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return ReportSaveDTO.builder()
                .iban(account.getAccountReference().getIban())
                .bban(account.getAccountReference().getBban())
                .sortCodeAccountNumber(account.getAccountReference().getSortCodeAccountNumber())
                .maskedPan(account.getAccountReference().getMaskedPan())
                .initialBalance(account.getBalance())
                .lastDataFetchTime(account.getLastDataFetchTime())
                .currency(account.getCurrency())
                .newestTransactionDate(getNewestTransaction(transactions))
                .oldestTransactionDate(getOldestTransaction(transactions))
                .creditScoreMonthly(creditScoreMonthlyReports)
                .creditLimit(account.getCreditLimit())
                .accountHolder(account.getAccountHolder())
                .transactionsSize(transactions.size())
                .build();
    }

    private static Map<LocalDate, List<CreditScoreTransactionDTO>> groupTransactionsPerMonth(List<CreditScoreTransactionDTO> transactions) {
        return transactions.stream()
                .collect(groupingBy(transaction -> transaction.getDate().withDayOfMonth(1)));
    }

    private static MonthlyReportSaveDTO calculateReportForGivenMonth(LocalDate date,
                                                                     List<CreditScoreTransactionDTO> monthlyTransactions,
                                                                     List<BalanceHistory> balanceHistoryForTransactions) {
        MonthlyMinMaxAverageBalance monthlyMinMaxAverageBalance = BalanceHistoryUtil.calculateMaxAndMinBalanceForMonth(
                balanceHistoryForTransactions, date);

        return MonthlyReportSaveDTO.builder()
                .year(date.getYear())
                .month(date.getMonthValue())
                .highestBalance(monthlyMinMaxAverageBalance.getMax())
                .lowestBalance(monthlyMinMaxAverageBalance.getMin())
                .averageBalance(monthlyMinMaxAverageBalance.getAverage())
                .categoriesAmounts(calculateMonthlyReportPerCategory(monthlyTransactions))
                .incomingTransactionsSize(countIncomingTransactions(monthlyTransactions))
                .outgoingTransactionsSize(countOutgoingTransactions(monthlyTransactions))
                .build();
    }

    private static List<MonthlyCategoryReportSaveDTO> calculateMonthlyReportPerCategory(List<CreditScoreTransactionDTO> monthlyTransactions) {
        Map<Category, BigDecimal> amountPerCategory = monthlyTransactions.stream()
                .collect(Collectors.toMap(
                        CreditScoreTransactionDTO::getCreditScoreTransactionCategory,
                        CreditScoreTransactionDTO::getAmount,
                        BigDecimal::add));

        Map<Category, Integer> transactionsPerCategory = new EnumMap<>(Category.class);

        for (Category category :amountPerCategory.keySet()) {
            var total = monthlyTransactions.stream()
                    .filter(creditScoreTransactionDTO -> category == creditScoreTransactionDTO.getCreditScoreTransactionCategory())
                    .count();
            transactionsPerCategory.put(category, Math.toIntExact(total));
        }

        return amountPerCategory.entrySet().stream()
                .map(entry -> MonthlyCategoryReportSaveDTO.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue().abs())
                        .totalTransactions(transactionsPerCategory.getOrDefault(entry.getKey(), 0))
                        .build())
                .sorted(Comparator.comparing(monthlyCategoryReport -> monthlyCategoryReport.getCategory().getValue()))
                .toList();
    }

    private static LocalDate getOldestTransaction(List<CreditScoreTransactionDTO> transactions) {
        return transactions.stream()
                .map(CreditScoreTransactionDTO::getDate)
                .min(LocalDate::compareTo)
                .orElseThrow();
    }

    private static LocalDate getNewestTransaction(List<CreditScoreTransactionDTO> transactions) {
        return transactions.stream()
                .map(CreditScoreTransactionDTO::getDate)
                .max(LocalDate::compareTo)
                .orElseThrow();
    }

    private static int countIncomingTransactions(List<CreditScoreTransactionDTO> transactions) {
        return Math.toIntExact(transactions.stream()
                .filter(CreditScoreTransactionDTO::isIncoming)
                .count());
    }

    private static int countOutgoingTransactions(List<CreditScoreTransactionDTO> transactions) {
        return Math.toIntExact(transactions.stream()
                .filter(CreditScoreTransactionDTO::isOutgoing)
                .count());
    }
}
