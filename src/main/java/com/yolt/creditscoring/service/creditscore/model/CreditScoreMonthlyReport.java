package com.yolt.creditscoring.service.creditscore.model;

import com.yolt.creditscoring.common.signature.SignatureCreditScoreMonthlyReport;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = CreditScoreMonthlyReport.TABLE_NAME)
@Entity
@Builder
public class CreditScoreMonthlyReport implements SignatureCreditScoreMonthlyReport {

    public static final String TABLE_NAME = "credit_score_monthly_report";

    @Id
    private UUID id;

    private Integer year;

    private Integer month;

    private BigDecimal highestBalance;

    private BigDecimal lowestBalance;

    private BigDecimal averageBalance;

    private Integer incomingTransactionsSize;

    private Integer outgoingTransactionsSize;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumns({
            @JoinColumn(name = "credit_score_monthly_report_id", referencedColumnName = "id")})
    @MapKey(name = "category")
    private Map<Category, CategorizedAmountEntity> categorizedAmounts;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "credit_score_report_id")
    private CreditScoreReport creditScoreReport;

    @Override
    public BigDecimal getTotalIncoming() {
        return categorizedAmounts.values().stream()
                .filter(categorizedAmountEntity ->
                        Category.isIncomeCategory(categorizedAmountEntity.getCategory()))
                .map(CategorizedAmountEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalOutgoing() {
        return categorizedAmounts.values().stream()
                .filter(categorizedAmountEntity ->
                        Category.isExpenseCategory(categorizedAmountEntity.getCategory()))
                .map(CategorizedAmountEntity::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static class CreditScoreMonthlyReportBuilder {

        public CreditScoreMonthlyReportBuilder categorizedAmount(Category category, BigDecimal amount, int transactionTotal) {
            this.categorizedAmounts = Objects.requireNonNullElse(categorizedAmounts, new HashMap<>());
            this.categorizedAmounts.put(category, new CategorizedAmountEntity(UUID.randomUUID(), amount, transactionTotal, category, null));
            return this;
        }
    }

}
