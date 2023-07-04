package com.yolt.creditscoring.service.creditscore.model;

import com.yolt.creditscoring.service.creditscore.category.SMECategoryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public enum Category {
    LOANS("Loans", SMECategoryType.INCOMING),
    EQUITY_FINANCING("Equity Financing", SMECategoryType.INCOMING),
    REVENUE("Revenue", SMECategoryType.INCOMING),
    TAX_RETURNS("Tax Returns", SMECategoryType.INCOMING),
    OTHER_INCOME("Other Income", SMECategoryType.INCOMING),

    INTEREST_AND_REPAYMENTS("Interest and Repayments", SMECategoryType.OUTGOING),
    INVESTMENTS("Investments", SMECategoryType.OUTGOING),
    FOOD_AND_DRINKS("Food and Drinks", SMECategoryType.OUTGOING),
    VEHICLE_AND_DRIVING_EXPENSES("Vehicles and Driving Expenses", SMECategoryType.OUTGOING),
    RENT_AND_FACILITIES("Rent and Facilities", SMECategoryType.OUTGOING),
    TRAVEL_EXPENSES("Travel Expenses", SMECategoryType.OUTGOING),
    MARKETING_AND_PROMOTION("Marketing and Promotion", SMECategoryType.OUTGOING),
    OTHER_OPERATING_COSTS("Other Operating Costs", SMECategoryType.OUTGOING),
    UTILITIES("Utilities", SMECategoryType.OUTGOING),
    COLLECTION_COSTS("Collection Costs", SMECategoryType.OUTGOING),
    SALARIES("Salaries", SMECategoryType.OUTGOING),
    PENSION_PAYMENTS("Pension Payments", SMECategoryType.OUTGOING),
    CORPORATE_SAVINGS_DEPOSITS("Corporate Savings Deposits", SMECategoryType.OUTGOING),
    EQUITY_WITHDRAWAL("Equity Withdrawal", SMECategoryType.OUTGOING),
    SALES_TAX("Sales Tax", SMECategoryType.OUTGOING),
    PAYROLL_TAX("Payroll Tax", SMECategoryType.OUTGOING),
    CORPORATE_INCOME_TAX("Corporate Income Tax", SMECategoryType.OUTGOING),
    UNSPECIFIED_TAX("Unspecified Tax", SMECategoryType.OUTGOING),
    OTHER_EXPENSES("Other Expenses", SMECategoryType.OUTGOING);

    private final String value;
    private final SMECategoryType smeCategoryType;

    public static Category fromString(String categoryText, BigDecimal amount) {
        for (Category category : Category.values()) {
            if (category.value.equalsIgnoreCase(categoryText)) {
                return category;
            }
        }

        if(amount.compareTo(BigDecimal.ZERO) > 0) {
            return OTHER_INCOME;
        } else {
            return OTHER_EXPENSES;
        }
    }

    public static boolean isIncomeCategory(Category category) {
        return category.smeCategoryType == SMECategoryType.INCOMING;
    }

    public static boolean isExpenseCategory(Category category) {
        return !isIncomeCategory(category);
    }
}
