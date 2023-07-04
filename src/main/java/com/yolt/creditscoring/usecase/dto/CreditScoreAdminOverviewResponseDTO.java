package com.yolt.creditscoring.usecase.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

@Value
@Builder
public class CreditScoreAdminOverviewResponseDTO {

    @PositiveOrZero
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal averageRecurringIncome;

    @PositiveOrZero
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal averageRecurringCosts;

    LocalDate startDate;

    LocalDate endDate;

    Integer incomingTransactionsSize;

    Integer outgoingTransactionsSize;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal monthlyAverageIncome;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal monthlyAverageCost;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalIncomeAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalOutgoingAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal averageIncomeTransactionAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal averageOutcomeTransactionAmount;

    Integer vatTotalPayments;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal vatAverage;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalCorporateTax;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalTaxReturns;
}
