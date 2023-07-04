package com.yolt.creditscoring.usecase.dto;

import com.yolt.creditscoring.service.creditscore.storage.dto.response.admin.BankAccountDetailsDTO;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreditScoreReportAdminOverviewResponseDTO {

    CreditScoreAdminOverviewResponseDTO creditScoreOverview;
    BankAccountDetailsDTO accountDetails;
}
