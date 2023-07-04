package com.yolt.creditscoring.service.creditscore.model;

import com.yolt.creditscoring.common.signature.SignatureCreditScoreReport;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = CreditScoreReport.TABLE_NAME)
@Entity
@Builder
public class CreditScoreReport implements SignatureCreditScoreReport {

    public static final String TABLE_NAME = "credit_score_report";

    @Id
    private UUID id;

    private UUID creditScoreUserId;

    @Embedded
    private AccountReference accountReference;

    private BigDecimal initialBalance;

    private OffsetDateTime lastDataFetchTime;

    private String currency;

    private LocalDate newestTransactionDate;

    private LocalDate oldestTransactionDate;

    @NegativeOrZero
    private BigDecimal creditLimit;

    @PositiveOrZero
    private Integer transactionsSize;

    @NotBlank
    private String signature;

    @NotNull
    private UUID signatureKeyId;

    @NotNull
    @OrderColumn
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> signatureJsonPaths;

    private String accountHolder;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "creditScoreReport", cascade = CascadeType.ALL)
    private Set<CreditScoreMonthlyReport> creditScoreMonthly;

    @Override
    public UUID getUserId() {
        return creditScoreUserId;
    }

    @Override
    public String getIban() {
        return accountReference.getIban();
    }

    @Override
    public String getBban() {
        return accountReference.getBban();
    }

    @Override
    public String getMaskedPan() {
        return accountReference.getMaskedPan();
    }

    @Override
    public String getSortCodeAccountNumber() {
        return accountReference.getSortCodeAccountNumber();
    }
}
