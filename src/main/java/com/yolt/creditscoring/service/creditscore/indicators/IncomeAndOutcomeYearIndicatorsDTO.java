package com.yolt.creditscoring.service.creditscore.indicators;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class IncomeAndOutcomeYearIndicatorsDTO {

    LocalDate startDate;

    LocalDate endDate;

    Integer incomingTransactionsSize;

    Integer outgoingTransactionsSize;

    BigDecimal monthlyAverageIncome;

    BigDecimal monthlyAverageCost;

    BigDecimal totalIncomeAmount;

    BigDecimal totalOutgoingAmount;

    BigDecimal averageIncomeTransactionAmount;

    BigDecimal averageOutcomeTransactionAmount;
}
