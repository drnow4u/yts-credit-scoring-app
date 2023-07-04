package com.yolt.creditscoring.usecase.dto;

import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.MonthlyAdminReportDTO;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

@Value
@Builder
public class CreditScoreAdminMonthsResponseDTO {

    private Set<MonthlyAdminReportDTO> monthlyReports;
}
