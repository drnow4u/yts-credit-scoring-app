package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class UserSite {

    private ConnectionStatusEnum connectionStatus;

    private LastDataFetchFailureReasonEnum lastDataFetchFailureReason;

    private OffsetDateTime lastDataFetchTime;
}
