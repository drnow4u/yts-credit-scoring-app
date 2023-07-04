package com.yolt.creditscoring.common.signature;

import java.math.BigDecimal;

public interface SignatureCreditScoreMonthlyReport {
    Integer getMonth();

    Integer getYear();

    BigDecimal getHighestBalance();

    BigDecimal getLowestBalance();

    BigDecimal getTotalIncoming();

    BigDecimal getTotalOutgoing();

}
