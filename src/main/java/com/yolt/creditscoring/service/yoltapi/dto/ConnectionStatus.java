package com.yolt.creditscoring.service.yoltapi.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class ConnectionStatus {
    String connectionStatus; //TODO: Change to enumeration
    OffsetDateTime lastDataFetchTime;
}
