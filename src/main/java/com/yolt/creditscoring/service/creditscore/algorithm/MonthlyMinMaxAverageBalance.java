package com.yolt.creditscoring.service.creditscore.algorithm;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class MonthlyMinMaxAverageBalance {
    BigDecimal min;
    BigDecimal max;
    BigDecimal average;
}
