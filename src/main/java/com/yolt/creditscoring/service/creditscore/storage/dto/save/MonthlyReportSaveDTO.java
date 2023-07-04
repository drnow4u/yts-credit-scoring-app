package com.yolt.creditscoring.service.creditscore.storage.dto.save;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.common.signature.SignatureCreditScoreMonthlyReport;
import com.yolt.creditscoring.service.creditscore.model.Category;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class MonthlyReportSaveDTO implements SignatureCreditScoreMonthlyReport {

    private Integer month;

    private Integer year;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal highestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal lowestBalance;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal averageBalance;

    private Integer incomingTransactionsSize;

    private Integer outgoingTransactionsSize;

    @Singular
    private List<MonthlyCategoryReportSaveDTO> categoriesAmounts;

    @Override
    public BigDecimal getTotalIncoming() {
        return categoriesAmounts.stream()
                .filter(categoryReport -> Category.isIncomeCategory(categoryReport.getCategory()))
                .map(MonthlyCategoryReportSaveDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalOutgoing() {
        return categoriesAmounts.stream()
                .filter(categoryReport -> Category.isExpenseCategory(categoryReport.getCategory()))
                .map(MonthlyCategoryReportSaveDTO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
