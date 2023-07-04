package com.yolt.creditscoring.service.yoltapi.dto;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class LoginResponse {
    UUID activityId;
    String redirectUrl;
    String dataFetchFailureReason;
}
