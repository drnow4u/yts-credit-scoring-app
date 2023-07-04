package com.yolt.creditscoring.service.estimate.provider.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class EstimateTransactionDTO {

    UUID id;
    EstimateAmountDTO amount;
    String currencyCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate dateBooked;
}
