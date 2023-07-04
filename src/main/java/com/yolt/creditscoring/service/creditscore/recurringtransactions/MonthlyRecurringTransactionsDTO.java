package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Data
@Builder
public class MonthlyRecurringTransactionsDTO {

    @NonNull
    private Integer month;

    @NonNull
    private Integer year;

    @PositiveOrZero
    private BigDecimal incomeRecurringAmount;

    @PositiveOrZero
    private Integer incomeRecurringSize;

    @PositiveOrZero
    private BigDecimal outcomeRecurringAmount;

    @PositiveOrZero
    private Integer outcomeRecurringSize;
}
