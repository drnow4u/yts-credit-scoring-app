package com.yolt.creditscoring.common.signature;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public interface SignatureCreditScoreReport {
    UUID getUserId();

    String getIban();

    String getBban();

    String getMaskedPan();

    String getSortCodeAccountNumber();

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal getInitialBalance();

    OffsetDateTime getLastDataFetchTime();

    String getCurrency();

    LocalDate getNewestTransactionDate();

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal getCreditLimit();

    Integer getTransactionsSize();

    String getAccountHolder();
}
