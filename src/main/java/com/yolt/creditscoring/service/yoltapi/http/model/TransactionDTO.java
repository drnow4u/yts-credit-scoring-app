package com.yolt.creditscoring.service.yoltapi.http.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDTO {

    private BigDecimal amount;

    private LocalDate bookingDate;

    private CurrencyEnum currency;

    private LocalDate date;

    private EnrichmentDTO enrichment;
}
