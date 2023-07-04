import { renderHook } from "@testing-library/react-hooks";

import { useInOutChartData } from ".";

test("Should get correct data for incoming/outgoing chart", async () => {
  const monthlyReports = [
    {
      month: 10,
      year: 2021,
      isCompleteMonth: true,
      highestBalance: "1000.00",
      lowestBalance: "-800.00",
      averageBalance: "900.00",
      totalIncoming: "1000.01",
      totalOutgoing: "0.00",
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
      totalIncoming: "2000.00",
      totalOutgoing: "3000.00",
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

  const { result } = renderHook(() => useInOutChartData(monthlyReports));

  const inOutChartData = result.current;

  const incomingSeries = inOutChartData?.map((row) => ({
    totalIncoming: row.totalIncoming,
  }));

  expect(incomingSeries).toStrictEqual([
    { totalIncoming: "1000.01" },
    { totalIncoming: "2000.00" },
  ]);

  const outgoingSeries = inOutChartData?.map((row) => ({
    totalOutgoing: row.totalOutgoing,
  }));

  expect(outgoingSeries).toStrictEqual([
    { totalOutgoing: "0" },
    { totalOutgoing: "3000" },
  ]);
});

test("Should get correct data for incoming/outgoing chart with negative outgoings", async () => {
  const monthlyReports = [
    {
      month: 10,
      year: 2021,
      isCompleteMonth: true,
      highestBalance: "1000.00",
      lowestBalance: "-800.00",
      averageBalance: "900.00",
      totalIncoming: "0.01",
      totalOutgoing: "-1000.00",
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
      totalIncoming: "2000.00",
      totalOutgoing: "-3000.01",
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

  const { result } = renderHook(() => useInOutChartData(monthlyReports));

  const inOutChartData = result.current;

  const incomingSeries = inOutChartData?.map((row) => ({
    totalIncoming: row.totalIncoming,
  }));

  expect(incomingSeries).toStrictEqual([
    { totalIncoming: "0.01" },
    { totalIncoming: "2000.00" },
  ]);

  const outgoingSeries = inOutChartData?.map((row) => ({
    totalOutgoing: row.totalOutgoing,
  }));

  expect(outgoingSeries).toStrictEqual([
    { totalOutgoing: "1000" },
    { totalOutgoing: "3000.01" },
  ]);
});
