import { CreditReport } from "types/admin/creditReport";

import { mapDates } from ".";

test("Should get correct period range for credit report", async () => {
  const creditReport = {
    userId: "1e1d8270-b63c-488e-a556-0a211d9bd378",
    initialBalance: "123.4",
    oldestTransactionDate: "2021-01-15",
    newestTransactionDate: "2021-12-12",
    currency: "PLN",
    transactionsSize: 0,
    lastDataFetchTime: "2021-12-16T13:47:35.593Z",
  } as CreditReport;

  const monthlyReports = {
    monthlyReports: [
      {
        month: 12,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 10,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ],
  };

  const result = mapDates(creditReport, monthlyReports);

  const periods = result?.map((row) => ({
    period: row.period,
  }));

  expect(periods).toStrictEqual([
    { period: "15 - 31 Oct 2021" },
    { period: "Nov 2021" },
    { period: "1 - 12 Dec 2021" },
  ]);

  const monthCompletness = result?.map((row) => ({
    isCompleteMonth: row.isCompleteMonth,
  }));

  expect(monthCompletness).toStrictEqual([
    { isCompleteMonth: false },
    { isCompleteMonth: true },
    { isCompleteMonth: false },
  ]);
});

test("Should get correct period range for credit report when last data fetch is not equal", async () => {
  const creditReport = {
    userId: "1e1d8270-b63c-488e-a556-0a211d9bd378",
    initialBalance: "123.4",
    oldestTransactionDate: "2021-01-15",
    newestTransactionDate: "2021-12-12",
    currency: "PLN",
    transactionsSize: 0,
    lastDataFetchTime: "2023-10-16T13:47:35.593Z",
  } as CreditReport;

  const monthlyReports = {
    monthlyReports: [
      {
        month: 12,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 10,
        year: 2021,
        highestBalance: "1000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ],
  };

  const result = mapDates(creditReport, monthlyReports);

  const periods = result?.map((row) => ({
    period: row.period,
  }));

  expect(periods).toStrictEqual([
    { period: "15 - 31 Oct 2021" },
    { period: "Nov 2021" },
    { period: "Dec 2021" },
  ]);

  const monthCompletness = result?.map((row) => ({
    isCompleteMonth: row.isCompleteMonth,
  }));

  expect(monthCompletness).toStrictEqual([
    { isCompleteMonth: false },
    { isCompleteMonth: true },
    { isCompleteMonth: true },
  ]);
});
