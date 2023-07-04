package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RecurringAverageDTO {

    private BigDecimal incomeAverage;
    private BigDecimal outcomeAverage;
}
