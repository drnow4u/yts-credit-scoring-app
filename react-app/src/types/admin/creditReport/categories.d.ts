import type {
  CreditScoreTransactionCategoryString,
  CreditScoreTransactionCategoryType,
} from "helpers/creditReport";

export interface CreditScoreCategorisedResponse {
  totalTransactionAmount: string;
  averageTransactionAmount: string;
  categoryName: CreditScoreTransactionCategoryString;
  categoryType: CreditScoreTransactionCategoryType;
  totalTransactions: number;
}
