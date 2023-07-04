package com.yolt.creditscoring.service.creditscore.storage.dto.response.admin;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class ReportDownloadDataDTO {

    private TogglesDTO toggles;
    private String currency;
}
