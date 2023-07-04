import faker from "@faker-js/faker";
import {
  CreditReport,
  CreditReportResult,
  CreditScoreResponse,
} from "types/credit-report";

interface CreditReportGeneratorOptions {
  iban: string;
  initialBalance: string;
  newestTransactionDate: string;
  oldestTransactionDate: string;
  creditLimit: string;
  transactionsSize: number;
  accountHolder: string;
  creditScoreMonthly: CreditReportResult[];
}

interface CreditScoreGeneratorOptions {
  year: number;
  highestBalance: string;
  lowestBalance: string;
  averageBalance: string;
  totalIncoming: string;
  totalOutgoing: string;
}

export function createCreditScoreResponse(
  options?: Partial<CreditReportGeneratorOptions>
): CreditScoreResponse {
  return {
    report: getCreditReport(options),
    additionalTextReport:
      "This is example client specific message. We will contact you soon.",
    userEmail: faker.internet.email(),
  };
}

export function getCreditReport(
  options?: Partial<CreditReportGeneratorOptions>
): CreditReport {
  const creditReportGeneratorDefaultOptions: CreditReportGeneratorOptions = {
    iban: faker.finance.iban(false),
    initialBalance: faker.finance.amount(0, 9999).toString(),
    newestTransactionDate: faker.date.recent(10).toISOString(),
    oldestTransactionDate: faker.date.past(1).toISOString(),
    creditLimit: faker.finance.amount(1000, 9999).toString(),
    transactionsSize: faker.datatype.number(100),
    accountHolder: faker.name.findName(),
    creditScoreMonthly: [
      createCreditScoreResult(1),
      createCreditScoreResult(2),
      createCreditScoreResult(3),
      createCreditScoreResult(4),
      createCreditScoreResult(5),
      createCreditScoreResult(6),
      createCreditScoreResult(7),
      createCreditScoreResult(8),
      createCreditScoreResult(9),
      createCreditScoreResult(10),
      createCreditScoreResult(11),
      createCreditScoreResult(12),
    ],
  };
  const report = {
    userId: "2180ed84-06f3-4f25-9c3b-38790690df0a",
    currency: "EUR",
    lastDataFetchTime: faker.date.recent(5).toISOString(),
    ...creditReportGeneratorDefaultOptions,
    ...options,
  };
  return report;
}

export function createCreditScoreResult(
  month: number,
  options?: Partial<CreditScoreGeneratorOptions>
): CreditReportResult {
  const lowestBalance = faker.finance.amount(-1000, 1000);
  const highestBalance = faker.finance.amount(1000, 9999);
  const creditScoreGeneratorDefaultOptions: CreditScoreGeneratorOptions = {
    year: faker.date.recent().getFullYear(),
    highestBalance: highestBalance.toString(),
    lowestBalance: lowestBalance.toString(),
    averageBalance: faker.finance
      .amount(Number(lowestBalance) + 1, Number(highestBalance) - 1)
      .toString(),
    totalIncoming: faker.finance.amount(1000, 9999).toString(),
    totalOutgoing: faker.finance.amount(1000, 9999).toString(),
  };
  return {
    month: month,
    ...creditScoreGeneratorDefaultOptions,
    ...options,
    incomingTransactionsSize: faker.datatype.number(200),
    outgoingTransactionsSize: faker.datatype.number(200),
  };
}
