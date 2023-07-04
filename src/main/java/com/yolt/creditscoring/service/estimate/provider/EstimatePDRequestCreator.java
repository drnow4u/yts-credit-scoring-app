package com.yolt.creditscoring.service.estimate.provider;

import com.yolt.creditscoring.service.estimate.provider.dto.EstimateAmountDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimatePDRequestDTO;
import com.yolt.creditscoring.service.estimate.provider.dto.EstimateTransactionDTO;
import com.yolt.creditscoring.service.estimate.provider.exception.EstimateAPIException;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreAccountDTO;
import com.yolt.creditscoring.service.yoltapi.dto.CreditScoreTransactionDTO;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@UtilityClass
public class EstimatePDRequestCreator {

    public EstimatePDRequestDTO createRequest(CreditScoreAccountDTO account) {
        return EstimatePDRequestDTO.builder()
                .referenceId(UUID.randomUUID())
                .currentBalance(amountMapper(account.getBalance()))
                .transactions(transactionMapper(account.getTransactions()))
                .build();
    }

    private List<EstimateTransactionDTO> transactionMapper(List<CreditScoreTransactionDTO> transactions) {
        LocalDate newestTransactionDate = transactions.stream()
                .map(CreditScoreTransactionDTO::getDate)
                .max(Comparator.comparing(LocalDate::toEpochDay))
                .orElseThrow(() -> new EstimateAPIException("Could not obtain the newest transaction date"));

        return transactions.stream()
                .filter(getFullSixMonthsOfTransaction(newestTransactionDate))
                .map(transaction -> EstimateTransactionDTO.builder()
                        .id(UUID.randomUUID())
                        .amount(amountMapper(transaction.getAmount()))
                        .currencyCode(transaction.getCurrency().getCurrencyCode())
                        .dateBooked(transaction.getDate())
                        .build())
                .toList();
    }

    /**
     * Calculate a credit score based on full six months of transaction data of an SME.
     * For example: Suppose the application date is 2021-04-09.
     * That would mean that the following months of transactions must be provided:
     * Oct, Nov and Dec 2020, Jan, Feb, Mar 2021. April 2021 would be incomplete.
     *
     * @param newestTransactionDate
     * @return
     */
    private Predicate<CreditScoreTransactionDTO> getFullSixMonthsOfTransaction(LocalDate newestTransactionDate) {
        return t ->
                t.getDate().isBefore(newestTransactionDate.withDayOfMonth(1)) &&
                        t.getDate().isAfter(newestTransactionDate.withDayOfMonth(1).minusMonths(6).minusDays(1));
    }

    private EstimateAmountDTO amountMapper(BigDecimal amount) {
        return EstimateAmountDTO.builder()
                .unscaledValue(amount.unscaledValue())
                .scale(amount.scale())
                .build();
    }
}
