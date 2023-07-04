package com.yolt.creditscoring.service.estimate.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.UUID;

@Value
@Builder
public class EstimatePDRequestDTO {

    UUID referenceId;
    @JsonProperty("currentBalance")
    EstimateAmountDTO currentBalance;
    List<EstimateTransactionDTO> transactions;
}
