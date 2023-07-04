package com.yolt.creditscoring.service.creditscore.recurringtransactions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

import static com.yolt.creditscoring.service.creditscore.recurringtransactions.RecurringTransactionsMonthlyReportEntity.TABLE_NAME;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TABLE_NAME)
@Builder
public class RecurringTransactionsMonthlyReportEntity {

    public static final String TABLE_NAME = "credit_score_monthly_recurring_transactions_report";

    @Id
    private UUID id;

    private UUID creditScoreId;

    private Integer month;

    private Integer year;

    private BigDecimal incomeRecurringAmount;

    private Integer incomeRecurringSize;

    private BigDecimal outcomeRecurringAmount;

    private Integer outcomeRecurringSize;
}
