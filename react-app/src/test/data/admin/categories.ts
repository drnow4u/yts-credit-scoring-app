import faker from "@faker-js/faker";
import { CreditScoreCategorisedResponse } from "types/admin/creditReport";

import {
  categoryTypeFromName,
  CreditScoreTransactionCategory,
  CreditScoreTransactionCategoryString,
} from "helpers/creditReport";

export function createCreditScoreCategorisedResponse(): CreditScoreCategorisedResponse[] {
  const result: CreditScoreCategorisedResponse[] = [];
  Object.values(CreditScoreTransactionCategory).forEach(
    (category: CreditScoreTransactionCategoryString) => {
      if (Math.random() > 0.5) {
        result.push({
          categoryName: category,
          categoryType: categoryTypeFromName(category),
          totalTransactions: faker.datatype.number(20),
          averageTransactionAmount: faker.finance.amount(100, 300),
          totalTransactionAmount: faker.finance.amount(300, 1000),
        });
      }
    }
  );
  return result;
}
