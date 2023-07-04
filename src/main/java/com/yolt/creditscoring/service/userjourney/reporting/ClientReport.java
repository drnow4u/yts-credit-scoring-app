package com.yolt.creditscoring.service.userjourney.reporting;

import com.yolt.creditscoring.service.userjourney.JourneyStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClientReport {

    String clientName;
    JourneyStatus status;
    int count;
}
