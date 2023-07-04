package com.yolt.creditscoring.service.userjourney;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientMetricsDTO {

    private int year;
    private int month;
    private JourneyStatus status;
    private int count;
}
