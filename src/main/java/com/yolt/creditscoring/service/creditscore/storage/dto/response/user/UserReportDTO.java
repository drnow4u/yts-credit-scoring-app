package com.yolt.creditscoring.service.creditscore.storage.dto.response.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NegativeOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@Builder
public class UserReportDTO {

    private UUID userId;

    private String iban;

    private String bban;

    private String maskedPan;

    private String sortCodeAccountNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal initialBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private OffsetDateTime lastDataFetchTime;

    private String currency;

    private LocalDate newestTransactionDate;

    private LocalDate oldestTransactionDate;

    @NegativeOrZero
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal creditLimit;

    private String accountHolder;

}
