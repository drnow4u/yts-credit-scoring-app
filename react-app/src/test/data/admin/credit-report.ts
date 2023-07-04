import faker from "@faker-js/faker";
import { CreditReport, CreditScoreResponse } from "types/admin/creditReport";

interface CreditReportAdminGeneratorOptions {
  iban: string;
  initialBalance: string;
  newestTransactionDate: string;
  oldestTransactionDate: string;
  creditLimit: string;
  transactionsSize: number;
  accountHolder: string;
}

export function createCreditScoreAdminResponse(
  options?: Partial<CreditReportAdminGeneratorOptions>
): CreditScoreResponse {
  return {
    adminReport: getCreditReportAdmin(options),
    userEmail: faker.internet.email(),
    publicKey:
      "vGO3eU16ag9zRkJ4AK8ZUZrjbtp5xWK0LyFMNT8933evJoHeczexMUzSiXaLrEFSyQZortk81zJH3y41MBO_UFDO_X0crAquNrkjZDrf9Scc5-MdxlWU2Jl7Gc4Z18AC9aNibWVmXhgvHYkEoFdLCFG-2Sq-qIyW4KFkjan05IE",
    signature: "signature",
    signatureJsonPaths: [
      "$['iban']",
      "$['initialBalance']",
      "$['newestTransactionDate']",
      "$['oldestTransactionDate']",
      "$['creditLimit']",
      "$['transactionsSize']",
      "$['creditScoreMonthly'][0]['month']",
      "$['creditScoreMonthly'][0]['year']",
      "$['creditScoreMonthly'][0]['highestBalance']",
      "$['creditScoreMonthly'][0]['lowestBalance']",
      "$['creditScoreMonthly'][0]['averageBalance']",
      "$['creditScoreMonthly'][0]['totalIncoming']",
      "$['creditScoreMonthly'][0]['totalOutgoing']",
    ],
    shouldVerifiedSignature: true,
    isSignatureVerified: false,
  };
}

export function getCreditReportAdmin(
  options?: Partial<CreditReportAdminGeneratorOptions>
): CreditReport {
  const creditReportGeneratorDefaultOptions: CreditReportAdminGeneratorOptions =
    {
      iban: faker.finance.iban(false),
      initialBalance: faker.finance.amount(0, 9999).toString(),
      newestTransactionDate: faker.date.recent(10).toISOString(),
      oldestTransactionDate: faker.date.past(1).toISOString(),
      creditLimit: faker.finance.amount(1000, 9999).toString(),
      transactionsSize: faker.datatype.number(100),
      accountHolder: faker.name.findName(),
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
