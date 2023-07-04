package com.yolt.creditscoring.service.creditscore.algorithm;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
class BalanceHistory {
    LocalDate date;
    BigDecimal amountBeforeTransaction;
}
