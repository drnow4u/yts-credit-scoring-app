package com.yolt.creditscoring.service.yoltapi.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class CreditScoreTransactionCycleDTO {
    @NonNull
    UUID cycleId;
    @NonNull
    BigDecimal amount;
    @NonNull
    CycleType cycleType;
}
