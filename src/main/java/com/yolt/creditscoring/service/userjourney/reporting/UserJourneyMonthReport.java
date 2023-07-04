package com.yolt.creditscoring.service.userjourney.reporting;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder
public class UserJourneyMonthReport {
    List<ClientReport> reports;
    OffsetDateTime from;
    OffsetDateTime till;
}
