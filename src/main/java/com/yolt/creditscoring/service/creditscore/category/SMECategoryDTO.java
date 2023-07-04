package com.yolt.creditscoring.service.creditscore.category;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yolt.creditscoring.service.creditscore.model.Category;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@RequiredArgsConstructor
public class SMECategoryDTO {

    Category categoryName;

    SMECategoryType categoryType;

    Integer totalTransactions;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal averageTransactionAmount;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    BigDecimal totalTransactionAmount;
}
