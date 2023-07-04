package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.util.UUID;

@Data
public class EnrichmentDTO {

    private String categorySME;

    private UUID cycleId;
}
