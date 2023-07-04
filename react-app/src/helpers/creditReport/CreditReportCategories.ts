import { CreditScoreCategorisedResponse } from "types/admin/creditReport";

export enum CreditScoreIncomesCategory {
  REVENUE = "REVENUE",
  EQUITY_FINANCING = "EQUITY_FINANCING",
  LOANS = "LOANS",
  TAX_RETURNS = "TAX_RETURNS",
  OTHER_INCOME = "OTHER_INCOME",
}

const CreditScoreIncomesCategoryOrder = Object.values(
  CreditScoreIncomesCategory
);

const IncomesCategorySort = (
  a: CreditScoreIncomesCategory,
  b: CreditScoreIncomesCategory
) =>
  CreditScoreIncomesCategoryOrder.indexOf(a) -
  CreditScoreIncomesCategoryOrder.indexOf(b);

export enum CreditScoreExpensesCategory {
  INTEREST_AND_REPAYMENTS = "INTEREST_AND_REPAYMENTS",
  EQUITY_WITHDRAWAL = "EQUITY_WITHDRAWAL",
  CORPORATE_SAVINGS_DEPOSITS = "CORPORATE_SAVINGS_DEPOSITS",
  INVESTMENTS = "INVESTMENTS",
  RENT_AND_FACILITIES = "RENT_AND_FACILITIES",
  VEHICLE_AND_DRIVING_EXPENSES = "VEHICLE_AND_DRIVING_EXPENSES",
  TRAVEL_EXPENSES = "TRAVEL_EXPENSES",
  FOOD_AND_DRINKS = "FOOD_AND_DRINKS",
  MARKETING_AND_PROMOTION = "MARKETING_AND_PROMOTION",
  UTILITIES = "UTILITIES",
  OTHER_OPERATING_COSTS = "OTHER_OPERATING_COSTS",
  COLLECTION_COSTS = "COLLECTION_COSTS",
  SALARIES = "SALARIES",
  PENSION_PAYMENTS = "PENSION_PAYMENTS",
  PAYROLL_TAX = "PAYROLL_TAX",
  SALES_TAX = "SALES_TAX",
  CORPORATE_INCOME_TAX = "CORPORATE_INCOME_TAX",
  UNSPECIFIED_TAX = "UNSPECIFIED_TAX",
  OTHER_EXPENSES = "OTHER_EXPENSES",
}

const CreditScoreExpensesCategoryOrder = Object.values(
  CreditScoreExpensesCategory
);

const ExpensesCategorySort = (
  a: CreditScoreExpensesCategory,
  b: CreditScoreExpensesCategory
) =>
  CreditScoreExpensesCategoryOrder.indexOf(a) -
  CreditScoreExpensesCategoryOrder.indexOf(b);

export const CreditScoreTransactionCategory = {
  ...CreditScoreIncomesCategory,
  ...CreditScoreExpensesCategory,
};

export type CreditScoreTransactionCategoryKind =
  typeof CreditScoreTransactionCategory;

export type CreditScoreTransactionCategoryString =
  keyof CreditScoreTransactionCategoryKind;

export type CreditScoreTransactionCategoryType = "OUTGOING" | "INCOMING";

export const categoryTypeFromName = (
  category: CreditScoreTransactionCategoryString
): CreditScoreTransactionCategoryType =>
  category in CreditScoreIncomesCategory ? "INCOMING" : "OUTGOING";

export interface CreditScoreIncomesCategorisedResponse
  extends Omit<
    CreditScoreCategorisedResponse,
    "categoryType" | "categoryName"
  > {
  categoryType: "INCOMING";
  categoryName: keyof typeof CreditScoreIncomesCategory;
}

export interface CreditScoreExpensesCategorisedResponse
  extends Omit<
    CreditScoreCategorisedResponse,
    "categoryType" | "categoryName"
  > {
  categoryType: "OUTGOING";
  categoryName: keyof typeof CreditScoreExpensesCategory;
}

export function filterByCategoryType<
  T =
    | CreditScoreIncomesCategorisedResponse
    | CreditScoreExpensesCategorisedResponse
>(
  type: CreditScoreTransactionCategoryType,
  data: CreditScoreCategorisedResponse[]
): T[] {
  //workaround for TS type control limitations
  return data.filter((value) => value.categoryType === type) as unknown as T[];
}

export const sortIncomes = (data: CreditScoreIncomesCategorisedResponse[]) =>
  data.sort((a, b) =>
    IncomesCategorySort(
      CreditScoreIncomesCategory[a.categoryName],
      CreditScoreIncomesCategory[b.categoryName]
    )
  );
export const sortExpenses = (data: CreditScoreExpensesCategorisedResponse[]) =>
  data.sort((a, b) =>
    ExpensesCategorySort(
      CreditScoreExpensesCategory[a.categoryName],
      CreditScoreExpensesCategory[b.categoryName]
    )
  );
