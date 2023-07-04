package com.yolt.creditscoring.usecase.dto;

import com.yolt.creditscoring.service.creditscore.storage.dto.response.user.UserReportDTO;
import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.NotNull;

@Value
@Builder(toBuilder = true)
public class CreditScoreUserResponseDTO {

    @NotNull
    UserReportDTO report;
    String additionalTextReport;
    String userEmail;
}
