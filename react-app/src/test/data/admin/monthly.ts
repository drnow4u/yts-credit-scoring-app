import faker from "@faker-js/faker";
import {
  CreditScoreMonthlyResponse,
  CreditScoreMonthResult,
} from "types/admin/creditReport";

interface CreditScoreAdminGeneratorOptions {
  year: number;
  highestBalance: string;
  lowestBalance: string;
  averageBalance: string;
  totalIncoming: string;
  totalOutgoing: string;
}

export function createCreditScoreAdminResult(
  month: number,
  options?: Partial<CreditScoreAdminGeneratorOptions>
): CreditScoreMonthResult {
  const totalIncoming =
    options?.totalIncoming ?? faker.finance.amount(1000, 9999).toString();
  const totalOutgoing =
    options?.totalOutgoing ?? faker.finance.amount(1000, 9999).toString();
  const lowestBalance = faker.finance.amount(-1000, 1000);
  const highestBalance = faker.finance.amount(1000, 9999);
  const creditScoreGeneratorDefaultOptions: Omit<
    CreditScoreAdminGeneratorOptions,
    "totalIncoming" | "totalOutgoing"
  > = {
    year: faker.date.recent().getFullYear(),
    highestBalance: highestBalance.toString(),
    lowestBalance: lowestBalance.toString(),
    averageBalance: faker.finance
      .amount(Number(lowestBalance) + 1, Number(highestBalance) - 1)
      .toString(),
  };
  return {
    month: month,
    ...creditScoreGeneratorDefaultOptions,
    ...options,
    totalIncoming,
    totalOutgoing,
    incomingTransactionsSize: faker.datatype.number(200),
    outgoingTransactionsSize: faker.datatype.number(200),
  };
}

export function createCreditScoreMonthlyAdminResponse(): CreditScoreMonthlyResponse {
  return {
    monthlyReports: [
      createCreditScoreAdminResult(1),
      createCreditScoreAdminResult(2),
      createCreditScoreAdminResult(3),
      createCreditScoreAdminResult(4),
      createCreditScoreAdminResult(5),
      createCreditScoreAdminResult(6),
      createCreditScoreAdminResult(7),
      createCreditScoreAdminResult(8),
      createCreditScoreAdminResult(9),
      createCreditScoreAdminResult(10),
      createCreditScoreAdminResult(11),
      createCreditScoreAdminResult(12),
    ],
  };
}
