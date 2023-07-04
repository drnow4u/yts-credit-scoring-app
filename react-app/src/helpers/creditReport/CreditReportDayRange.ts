import { DateTime } from "luxon";
import {
  CreditReport,
  CreditScoreMonthlyResponse,
  CreditScoreMonthResult,
} from "types/admin/creditReport";

export function mapDates(
  creditReport: CreditReport,
  creditScoreMonthly: CreditScoreMonthlyResponse
): CreditScoreMonthResult[] | undefined {
  const creditScoreMonthlySorted = (
    creditScoreMonthly.monthlyReports
      ? [...creditScoreMonthly.monthlyReports]
      : undefined
  )?.sort(function (creditReport1, creditReport2) {
    return (
      new Date(creditReport1.year, creditReport1.month).valueOf() -
      new Date(creditReport2.year, creditReport2.month).valueOf()
    );
  });
  return creditScoreMonthlySorted?.map((result, index, creditScoreMonthly) => {
    return {
      ...result,
      isCompleteMonth: isMonthComplete(
        creditReport,
        index,
        creditScoreMonthly.length
      ),
      period: getPeriod(creditReport, result, index, creditScoreMonthly.length),
    };
  });
}

function getPeriod(
  creditReport: CreditReport,
  creditReportResult: CreditScoreMonthResult,
  index: number,
  length: number
): string {
  const period = new Date(
    creditReportResult.year,
    creditReportResult.month - 1
  ).toLocaleString("en", { month: "short", year: "numeric" });

  if (index === length - 1) {
    return getDayRangeForNewestMonth(creditReport) + period;
  } else if (index === 0) {
    return getDayRangeForOldestMonth(creditReport) + period;
  } else {
    return period;
  }
}

function isMonthComplete(
  creditReport: CreditReport,
  index: number,
  length: number
): boolean {
  if (index === length - 1) {
    if (!!creditReport.newestTransactionDate) {
      return isNewestMonthComplete(
        creditReport.newestTransactionDate,
        creditReport.lastDataFetchTime
      );
    } else {
      return true;
    }
  } else if (index === 0) {
    return !!!creditReport.oldestTransactionDate;
  } else {
    return true;
  }
}

function isNewestMonthComplete(
  newestTransactionDate: string,
  lastDataFetchTime: string
): boolean {
  const newestDate = DateTime.fromISO(newestTransactionDate);
  const fetchDate = DateTime.fromISO(lastDataFetchTime);
  if (
    newestDate.hasSame(fetchDate, "year") &&
    newestDate.hasSame(fetchDate, "month")
  ) {
    return false;
  } else {
    return true;
  }
}

function getDayRangeForNewestMonth(creditReport: CreditReport) {
  if (!!creditReport.newestTransactionDate) {
    if (
      isNewestMonthComplete(
        creditReport.newestTransactionDate,
        creditReport.lastDataFetchTime
      )
    ) {
      return "";
    } else {
      const newestDate = DateTime.fromISO(creditReport.newestTransactionDate);
      return "1 - " + newestDate.day + " ";
    }
  } else {
    return "";
  }
}

function getDayRangeForOldestMonth(creditReport: CreditReport) {
  if (creditReport.oldestTransactionDate != null) {
    const oldestTransactionDate = new Date(creditReport.oldestTransactionDate);
    return (
      oldestTransactionDate.getDate() +
      " - " +
      new Date(
        oldestTransactionDate.getFullYear(),
        oldestTransactionDate.getMonth() + 1,
        0 // get last day of month
      ).getDate() +
      " "
    );
  }
  return "";
}
