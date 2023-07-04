import { renderHook } from "@testing-library/react-hooks";

import { useBalanceChartData } from ".";

test("Should get correct data for balance chart", async () => {
  const monthlyReports = [
    {
      month: 10,
      year: 2021,
      isCompleteMonth: true,
      highestBalance: "1000.00",
      lowestBalance: "-800.00",
      averageBalance: "900.00",
      totalIncoming: "1000.00",
      totalOutgoing: "1000.00",
      incomingTransactionsSize: 10,
      outgoingTransactionsSize: 10,
    },
    {
      month: 11,
      year: 2021,
      isCompleteMonth: true,
      highestBalance: "3000.00",
      lowestBalance: "2000.00",
      averageBalance: "2200.00",
      totalIncoming: "1000.00",
      totalOutgoing: "1000.00",
      incomingTransactionsSize: 10,
      outgoingTransactionsSize: 10,
    },
    {
      month: 12,
      year: 2021,
      isCompleteMonth: false,
      highestBalance: "2000.00",
      lowestBalance: "1000.00",
      averageBalance: "1300.00",
      totalIncoming: "1000.00",
      totalOutgoing: "1000.00",
      incomingTransactionsSize: 10,
      outgoingTransactionsSize: 10,
    },
  ];

  const { result } = renderHook(() => useBalanceChartData(monthlyReports));

  const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } = result.current;

  const balanceSeries = balanceChartData?.map((row) => ({
    balance: row.balance,
  }));

  expect(balanceSeries).toStrictEqual([
    { balance: ["-800.00", "1000.00"] },
    { balance: ["2000.00", "3000.00"] },
  ]);

  const averageSeries = balanceChartData?.map((row) => ({
    averageBalance: row.averageBalance,
  }));

  expect(averageSeries).toStrictEqual([
    { averageBalance: "900.00" },
    { averageBalance: "2200.00" },
  ]);

  expect(avgAvailable).toBeTruthy();
  expect(yAxisMin).toEqual(-1000);
  expect(yAxisMax).toEqual(10000);
});

describe("Should get correct data for balance chart without average balance", () => {
  test("Should get correct data for balance chart with zeros as average balance", async () => {
    const monthlyReports = [
      {
        month: 10,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "1000.00",
        lowestBalance: "-800.00",
        averageBalance: "0.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "3000.00",
        lowestBalance: "2000.00",
        averageBalance: "0.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 12,
        year: 2021,
        isCompleteMonth: false,
        highestBalance: "2000.00",
        lowestBalance: "1000.00",
        averageBalance: "0.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ];

    const { result } = renderHook(() => useBalanceChartData(monthlyReports));

    const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } =
      result.current;

    const balanceSeries = balanceChartData?.map((row) => ({
      balance: row.balance,
    }));

    expect(balanceSeries).toStrictEqual([
      { balance: ["-800.00", "1000.00"] },
      { balance: ["2000.00", "3000.00"] },
    ]);

    expect(avgAvailable).toBeFalsy();
    expect(yAxisMin).toEqual(-1000);
    expect(yAxisMax).toEqual(10000);
  });

  test("Should get correct data for balance chart without average balance", async () => {
    const monthlyReports = [
      {
        month: 10,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "1000.00",
        lowestBalance: "-800.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "3000.00",
        lowestBalance: "2000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 12,
        year: 2021,
        isCompleteMonth: false,
        highestBalance: "2000.00",
        lowestBalance: "1000.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ];

    const { result } = renderHook(() => useBalanceChartData(monthlyReports));

    const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } =
      result.current;

    const balanceSeries = balanceChartData?.map((row) => ({
      balance: row.balance,
    }));

    expect(balanceSeries).toStrictEqual([
      { balance: ["-800.00", "1000.00"] },
      { balance: ["2000.00", "3000.00"] },
    ]);

    expect(avgAvailable).toBeFalsy();
    expect(yAxisMin).toEqual(-1000);
    expect(yAxisMax).toEqual(10000);
  });

  test("Should get correct data for balance chart with empty average balance", async () => {
    const monthlyReports = [
      {
        month: 10,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "1000.00",
        lowestBalance: "-800.00",
        averageBalance: "",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "3000.00",
        lowestBalance: "2000.00",
        averageBalance: "",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 12,
        year: 2021,
        isCompleteMonth: false,
        highestBalance: "2000.00",
        lowestBalance: "1000.00",
        averageBalance: "",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ];

    // const result = mapDates(creditReport);
    const { result } = renderHook(() => useBalanceChartData(monthlyReports));

    const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } =
      result.current;

    const balanceSeries = balanceChartData?.map((row) => ({
      balance: row.balance,
    }));

    expect(balanceSeries).toStrictEqual([
      { balance: ["-800.00", "1000.00"] },
      { balance: ["2000.00", "3000.00"] },
    ]);

    expect(avgAvailable).toBeFalsy();
    expect(yAxisMin).toEqual(-1000);
    expect(yAxisMax).toEqual(10000);
  });

  test("Should get correct data for balance chart with broken average balance", async () => {
    const monthlyReports = [
      {
        month: 10,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "1000.00",
        lowestBalance: "-800.00",
        averageBalance: "test1",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 11,
        year: 2021,
        isCompleteMonth: true,
        highestBalance: "3000.00",
        lowestBalance: "2000.00",
        averageBalance: "0000",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
      {
        month: 12,
        year: 2021,
        isCompleteMonth: false,
        highestBalance: "2000.00",
        lowestBalance: "1000.00",
        averageBalance: "0.00",
        totalIncoming: "1000.00",
        totalOutgoing: "1000.00",
        incomingTransactionsSize: 10,
        outgoingTransactionsSize: 10,
      },
    ];

    const { result } = renderHook(() => useBalanceChartData(monthlyReports));

    const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } =
      result.current;

    const balanceSeries = balanceChartData?.map((row) => ({
      balance: row.balance,
    }));

    expect(balanceSeries).toStrictEqual([
      { balance: ["-800.00", "1000.00"] },
      { balance: ["2000.00", "3000.00"] },
    ]);

    expect(avgAvailable).toBeFalsy();
    expect(yAxisMin).toEqual(-1000);
    expect(yAxisMax).toEqual(10000);
  });
});
