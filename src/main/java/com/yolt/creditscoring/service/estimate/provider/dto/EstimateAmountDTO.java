package com.yolt.creditscoring.service.estimate.provider.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigInteger;

@Value
@Builder
public class EstimateAmountDTO {

    BigInteger unscaledValue;
    Integer scale;
}
