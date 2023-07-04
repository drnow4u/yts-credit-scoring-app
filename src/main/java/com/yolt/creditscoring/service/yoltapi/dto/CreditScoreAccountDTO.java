package com.yolt.creditscoring.service.yoltapi.dto;

import com.yolt.creditscoring.service.creditscore.model.AccountReference;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.validation.constraints.NegativeOrZero;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class CreditScoreAccountDTO {
    UUID id;
    BigDecimal balance;
    OffsetDateTime lastDataFetchTime;
    String currency;
    String status;
    String type;
    String usage;
    @NonNull
    AccountReference accountReference;
    @NegativeOrZero
    BigDecimal creditLimit;
    String accountHolder;
    List<CreditScoreTransactionDTO> transactions;
}
