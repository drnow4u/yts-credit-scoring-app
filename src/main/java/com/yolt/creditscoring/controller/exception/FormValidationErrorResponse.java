package com.yolt.creditscoring.controller.exception;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FormValidationErrorResponse {
    @Singular
    List<Violation> violations;

    @JsonCreator
    public FormValidationErrorResponse(@JsonProperty("violations") List<Violation> violations) {
        this.violations = violations;
    }

}
