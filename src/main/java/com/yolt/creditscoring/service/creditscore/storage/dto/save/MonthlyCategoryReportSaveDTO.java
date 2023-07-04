package com.yolt.creditscoring.service.creditscore.storage.dto.save;

import com.yolt.creditscoring.service.creditscore.model.Category;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthlyCategoryReportSaveDTO {

    private BigDecimal amount;
    private int totalTransactions;
    private Category category;
}
