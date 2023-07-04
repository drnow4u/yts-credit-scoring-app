package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class TransactionCycleDTO {

    private BigDecimal amount;

    private UUID cycleId;

    private CycleTypeEnum cycleType;
}
