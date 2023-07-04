import { createContext } from "react";

import { CreditScoreTransactionCategoryString } from ".";

export const CreditReportCategoriesContext = createContext<{
  selectedCategory: CreditScoreTransactionCategoryString | undefined;
  setSelectedCategory: (
    category: CreditScoreTransactionCategoryString | undefined,
    income: boolean
  ) => void;
}>({ selectedCategory: undefined, setSelectedCategory: () => {} });
