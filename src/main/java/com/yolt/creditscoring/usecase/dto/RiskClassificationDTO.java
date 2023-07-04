package com.yolt.creditscoring.usecase.dto;

import com.yolt.creditscoring.service.creditscore.model.PdStatus;
import com.yolt.creditscoring.service.estimate.provider.dto.RiskClassification;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record RiskClassificationDTO(
        @Max(10)
        @Min(0)
        Double rateLower,
        @Max(10)
        @Min(0)
        Double rateUpper,

        RiskClassification grade,
        @NotNull
        PdStatus status
) {
    public static RiskClassificationDTO createError(PdStatus status) {
        return new RiskClassificationDTO(null, null, null, status);
    }
}
