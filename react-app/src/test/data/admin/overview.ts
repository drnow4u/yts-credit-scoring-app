import faker from "@faker-js/faker";
import { CreditScoreOverviewResponse } from "types/admin/creditReport";

export function createCreditScoreOverviewAdminResponse(): CreditScoreOverviewResponse {
  return {
    incomingTransactionsSize: faker.datatype.number(100),
    outgoingTransactionsSize: faker.datatype.number(100),
    monthlyAverageIncome: faker.finance.amount(100, 999).toString(),
    monthlyAverageCost: faker.finance.amount(100, 999).toString(),
    totalIncomeAmount: faker.finance.amount(1000, 9999).toString(),
    totalOutgoingAmount: faker.finance.amount(1000, 9999).toString(),
    averageIncomeTransactionAmount: faker.finance.amount(100, 999).toString(),
    averageOutcomeTransactionAmount: faker.finance.amount(100, 999).toString(),
    startDate: "2020-12-01",
    endDate: "2021-11-30",
    vatTotalPayments: faker.datatype.number(100),
    vatAverage: faker.finance.amount(100, 999).toString(),
    totalCorporateTax: faker.finance.amount(1000, 9999).toString(),
    averageRecurringIncome: faker.finance.amount(1000, 9999).toString(), // TODO!!!
    averageRecurringCosts: faker.finance.amount(1000, 9999).toString(),
    totalTaxReturns: faker.finance.amount(1000, 9999).toString(),
  };
}
