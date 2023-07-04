package com.yolt.creditscoring.service.yoltapi.http.model;


import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class AccountDTO {

    private String accountHolder;

    private AccountReferencesDTO accountReferences;

    private BigDecimal balance;

    private CreditCardAccountDTO creditCardAccount;

    private CurrencyEnum currency;

    private CurrentAccountDTO currentAccount;

    private UUID id;

    private OffsetDateTime lastDataFetchTime;

    private AccountStatusEnum status;

    private TypeEnum type;

    private UsageEnum usage;
}
