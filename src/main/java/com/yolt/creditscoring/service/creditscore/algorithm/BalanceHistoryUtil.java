package com.yolt.creditscoring.service.creditscore.algorithm;

import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;

@UtilityClass
class BalanceHistoryUtil {

    public MonthlyMinMaxAverageBalance calculateMaxAndMinBalanceForMonth(List<BalanceHistory> balanceHistoryForTransactions,
                                                                         LocalDate date) {

        List<BigDecimal> balanceHistoryForGivenMonth = getBalancesBetweenOldestAndNewestTransactionsForMoth(
                balanceHistoryForTransactions, date);

        BigDecimal max = balanceHistoryForGivenMonth.stream()
                .max(Comparator.comparingDouble(BigDecimal::doubleValue))
                .orElseThrow();

        BigDecimal min = balanceHistoryForGivenMonth.stream()
                .min(Comparator.comparingDouble(BigDecimal::doubleValue))
                .orElseThrow();

        BigDecimal sum = balanceHistoryForGivenMonth.stream()
                .map(Objects::requireNonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = sum.divide(new BigDecimal(balanceHistoryForGivenMonth.size()), 2, RoundingMode.HALF_UP);

        return MonthlyMinMaxAverageBalance.builder()
                .min(min)
                .max(max)
                .average(average)
                .build();
    }

    public List<BalanceHistory> getBalanceHistoryForTransactions(BigDecimal initialBalance, List<CreditScoreTransactionDTO> transactions) {
        List<CreditScoreTransactionDTO> groupedTransactionAmountByDay = BalanceHistoryUtil.groupTransactionAmountByDay(transactions);

        List<CreditScoreTransactionDTO> transactionsSortedAndFilterForMonth = groupedTransactionAmountByDay.stream()
                .sorted(comparing(CreditScoreTransactionDTO::getDate).reversed())
                .toList();

        List<BalanceHistory> balanceHistories = new LinkedList<>();

        LocalDate lastDayOfMonthForFirstTransaction = transactionsSortedAndFilterForMonth.get(0).getDate().with(TemporalAdjusters.lastDayOfMonth());

        //set the initial balance as a first element of the list with date on last day of month for newest transaction
        balanceHistories.add(new BalanceHistory(lastDayOfMonthForFirstTransaction, initialBalance));

        LocalDate lastTransactionDay = lastDayOfMonthForFirstTransaction;
        for (CreditScoreTransactionDTO transactionDTO : transactionsSortedAndFilterForMonth) {
            //check if transaction for new month, if yes, add last balance from previous month as the newest in current month
            if (!lastTransactionDay.withDayOfMonth(1).isEqual(transactionDTO.getDate().withDayOfMonth(1))) {
                balanceHistories.add(new BalanceHistory(transactionDTO.getDate().with(TemporalAdjusters.lastDayOfMonth()), initialBalance));
            }
            balanceHistories.add(new BalanceHistory(transactionDTO.getDate(), initialBalance.subtract(transactionDTO.getAmount())));
            initialBalance = initialBalance.subtract(transactionDTO.getAmount());
            lastTransactionDay = transactionDTO.getDate();
        }

        return balanceHistories;
    }

    private List<BigDecimal> getBalancesBetweenOldestAndNewestTransactionsForMoth(List<BalanceHistory> balanceHistoryForTransactions,
                                                                                  LocalDate date) {
        return balanceHistoryForTransactions.stream()
                .filter(balanceHistory -> balanceHistory.getDate().withDayOfMonth(1).isEqual(date.withDayOfMonth(1)))
                .flatMap(balanceHistory -> Stream.of(balanceHistory.getAmountBeforeTransaction()))
                .toList();
    }

    /**
     * Reduce multiple transaction during given day into single transaction.
     * <p>
     * There is not always received from bank exact time of transaction during day.
     * In such case balance calculation has different value.
     * To bypass such situation balance calculation is on end of the day, not after each transaction.
     *
     * @param txList list of transactions
     * @return list of grouped transaction per day
     */
    private List<CreditScoreTransactionDTO> groupTransactionAmountByDay(List<CreditScoreTransactionDTO> txList) {
        Map<LocalDate, CreditScoreTransactionDTO> txGroupByDay = new HashMap<>();

        for (CreditScoreTransactionDTO transaction : txList) {
            if (txGroupByDay.containsKey(transaction.getDate())) {
                CreditScoreTransactionDTO creditScoreTransactionDTO = txGroupByDay.get(transaction.getDate());
                txGroupByDay.put(transaction.getDate(), CreditScoreTransactionDTO.builder()
                        .date(transaction.getDate())
                        .amount(creditScoreTransactionDTO.getAmount().add(transaction.getAmount()))
                        .build());
            } else {
                txGroupByDay.put(transaction.getDate(), transaction);
            }
        }

        return List.copyOf(txGroupByDay.values());
    }
}
