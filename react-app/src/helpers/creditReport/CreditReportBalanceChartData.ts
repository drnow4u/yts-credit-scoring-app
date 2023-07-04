import { useMemo } from "react";

import { useTranslation } from "react-i18next";
import { CreditScoreMonthResult } from "types/admin/creditReport";

import { useDateTimeFormatter } from "helpers/formatters";

interface BalanceChartData {
  month: string;
  balance: string[];
  averageBalance: string;
}

const sanitizeAverageBalance = (avgBalance: string | undefined): string => {
  //remove leading zeros
  const result = avgBalance?.replace(/^0+/, "");
  if (result === undefined || isNaN(Number(result)) || Number(result) === 0) {
    return "";
  } else {
    return result;
  }
};

// it should be already sorted asc in cache
const mapBalanceChartData = (
  scoreMonthly: CreditScoreMonthResult[],
  formatter: Intl.DateTimeFormat
): BalanceChartData[] | undefined => {
  return scoreMonthly
    ?.filter((row) => !!row.isCompleteMonth)
    ?.map((row) => ({
      month: formatter.format(new Date(row.year, row.month - 1)),
      balance: [row.lowestBalance, row.highestBalance],
      averageBalance: sanitizeAverageBalance(row.averageBalance),
    }));
};

const checkAverageBalanceAvailable = (data: BalanceChartData[]): boolean => {
  return data.reduce<boolean>(
    (prev, current) => prev || !!current.averageBalance,
    false
  );
};

export const useBalanceChartData = (creditReport: CreditScoreMonthResult[]) => {
  const { i18n } = useTranslation();
  const DateTimeFormatter = useDateTimeFormatter(i18n.language, {
    month: "short",
  });
  const balanceChartData: BalanceChartData[] | undefined = useMemo(() => {
    return mapBalanceChartData(creditReport, DateTimeFormatter);
  }, [creditReport, DateTimeFormatter]);

  const avgAvailable: boolean = useMemo(
    () =>
      balanceChartData ? checkAverageBalanceAvailable(balanceChartData) : false,
    [balanceChartData]
  );

  const yAxisMax = useMemo(() => {
    const max = creditReport
      ? Math.max(...creditReport.map((row) => Number(row.highestBalance)))
      : 0;
    return Math.ceil(max / 10000) * 10000;
  }, [creditReport]);

  const yAxisMin = useMemo(() => {
    const min = creditReport
      ? Math.min(...creditReport?.map((row) => Number(row.lowestBalance)))
      : 0;
    return Math.floor(min / 1000) * 1000;
  }, [creditReport]);

  return { balanceChartData, avgAvailable, yAxisMax, yAxisMin };
};
