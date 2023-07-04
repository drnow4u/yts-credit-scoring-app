package com.yolt.creditscoring.service.creditscore.category;

import com.yolt.creditscoring.service.creditscore.model.Category;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public interface GroupedUserCategories {

    @PositiveOrZero // This should be @Positive. For same old report is 0. Can be changed if future.
    @NotNull
    BigDecimal getTotalAmount();

    @Positive
    int getTransactionTotal();

    @PositiveOrZero
    float getAveragePerTransaction();

    Category getCategory();
}
