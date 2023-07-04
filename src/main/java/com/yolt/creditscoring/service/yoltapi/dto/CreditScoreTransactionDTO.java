package com.yolt.creditscoring.service.yoltapi.dto;

import com.yolt.creditscoring.service.creditscore.model.Category;
import lombok.Builder;
import lombok.Value;

import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Value
@Builder
public class CreditScoreTransactionDTO {

    LocalDate date;
    /**
     * positive - income
     * negative - outcome
     */
    BigDecimal amount;
    CurrencyUnit currency;
    Category creditScoreTransactionCategory;
    UUID cycleId;

    public static class CreditScoreTransactionDTOBuilder {
        public CreditScoreTransactionDTOBuilder currency(CurrencyUnit currency) {
            this.currency = currency;
            return this;
        }

        public CreditScoreTransactionDTOBuilder currency(String currency) {
            this.currency = Monetary.getCurrency(currency);
            return this;
        }
    }

    public boolean isIncoming() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isOutgoing() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }
}
