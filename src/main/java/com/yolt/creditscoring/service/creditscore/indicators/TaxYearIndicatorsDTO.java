package com.yolt.creditscoring.service.creditscore.indicators;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class TaxYearIndicatorsDTO {

    Integer vatTotalPayments;

    BigDecimal vatAverage;

    BigDecimal totalCorporateTax;

    BigDecimal totalTaxReturns;
}
