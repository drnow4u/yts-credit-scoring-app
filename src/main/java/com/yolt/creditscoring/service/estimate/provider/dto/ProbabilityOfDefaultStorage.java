package com.yolt.creditscoring.service.estimate.provider.dto;

import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Value
@Builder
public class ProbabilityOfDefaultStorage {

    @Min(0)
    @Max(100)
    Integer score;

    RiskClassification grade;
    @NotNull
    PdStatus status;
}
