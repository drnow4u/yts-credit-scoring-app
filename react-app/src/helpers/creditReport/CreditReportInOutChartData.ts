import { useMemo } from "react";

import { useTranslation } from "react-i18next";
import { CreditScoreMonthResult } from "types/admin/creditReport";

import { useDateTimeFormatter } from "helpers/formatters";

interface InOutChartData {
  month: string;
  totalIncoming: string;
  totalOutgoing: string;
}

// it should be already sorted asc in cache
const mapInOutChartData = (
  creditScoreMonthly: CreditScoreMonthResult[],
  formatter: Intl.DateTimeFormat
): InOutChartData[] | undefined => {
  return creditScoreMonthly
    ?.filter((row) => !!row.isCompleteMonth)
    ?.map((row) => ({
      month: formatter.format(new Date(row.year, row.month - 1)),
      totalIncoming: row.totalIncoming,
      totalOutgoing: Math.abs(Number(row.totalOutgoing)).toString(),
    }));
};

export const useInOutChartData = (
  creditScoreMonthly: CreditScoreMonthResult[]
) => {
  const { i18n } = useTranslation();
  const DateTimeFormatter = useDateTimeFormatter(i18n.language, {
    month: "short",
  });

  const inOutChartData: InOutChartData[] | undefined = useMemo(
    () => mapInOutChartData(creditScoreMonthly, DateTimeFormatter),
    [creditScoreMonthly, DateTimeFormatter]
  );

  return inOutChartData;
};
