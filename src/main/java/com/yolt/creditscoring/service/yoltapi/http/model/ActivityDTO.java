package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ActivityDTO {

    private UUID activityId;

    private OffsetDateTime endTime;
}
