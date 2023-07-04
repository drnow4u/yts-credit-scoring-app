package com.yolt.creditscoring.service.creditscore.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

import static com.yolt.creditscoring.service.creditscore.model.CategorizedAmountEntity.TABLE_NAME;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TABLE_NAME)
@Builder
public class CategorizedAmountEntity {

    public static final String TABLE_NAME = "credit_score_monthly_category_report";

    @Id
    private UUID id;

    @PositiveOrZero  // This should be @Positive. For same old report is 0. Can be changed if future.
    @NotNull
    private BigDecimal amount;

    @Positive
    @NotNull
    private int transactionTotal;

    @Enumerated(EnumType.STRING)
    private Category category;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "credit_score_monthly_report_id")
    private CreditScoreMonthlyReport creditScoreMonthlyReport;
}
