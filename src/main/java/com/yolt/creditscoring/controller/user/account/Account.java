package com.yolt.creditscoring.controller.user.account;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.UUID;

@Value
@Builder
public class Account {
    UUID id;
    BigDecimal balance;
    String currency;
    String accountNumber;
}
