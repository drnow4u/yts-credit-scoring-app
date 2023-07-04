package com.yolt.creditscoring.service.estimate.provider.dto;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class EstimateProbabilityOfDefaultDTO {

    @Min(0)
    @Max(100)
    @NotNull
    Integer score;
    @NotNull
    RiskClassification grade;
}
