package com.yolt.creditscoring.service.creditscore.storage.dto.response.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.common.signature.SignatureCreditScoreReport;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NegativeOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// Changes in this class impacts signature calculation for existing and new credit reports.
// 1. Adding new filed should work out of box.
// 2. To rename field migration is required for JSON paths e.g. https://git.yolt.io/backend/yts-credit-scoring-app/-/merge_requests/433/diffs#830f3653ba73bba5a3d1c9b1af515c4def7d69c6_0_1
// 3. To delete filed you need to check in DB with e.g. OTC is this field used in JSON path for already created report.
@Data
@Builder
public class BankAccountDetailsDTO implements SignatureCreditScoreReport {

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

    private Integer transactionsSize;

    private String accountHolder;
}
