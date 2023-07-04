import { useMemo } from "react";

import { Chakra } from "@yolt/design-system";
import { ReactComponent as CashInIcon } from "icons/cash-in.svg";
import { ReactComponent as CashOutIcon } from "icons/cash-out.svg";
import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";

import { SummaryCard } from "../SummaryCard";

const { SimpleGrid, Text } = Chakra;

interface YearlySummaryProps {
  averageRecurringIncome: string;
  averageRecurringCosts: string;
  monthlyAverageIncome: string;
  monthlyAverageCost: string;
  incomingTransactionsSize: number;
  outgoingTransactionsSize: number;
  totalIncomeAmount: string;
  totalOutgoingAmount: string;
  averageIncomeTransactionAmount: string;
  averageOutcomeTransactionAmount: string;
  startDate: string;
  endDate: string;
  vatTotalPayments: number;
  vatAverage: string;
  totalCorporateTax: string;
  totalTaxReturns: string;
}

export function YearlySummary({
  averageRecurringIncome,
  averageRecurringCosts,
  monthlyAverageIncome,
  monthlyAverageCost,
  incomingTransactionsSize,
  outgoingTransactionsSize,
  totalIncomeAmount,
  totalOutgoingAmount,
  averageIncomeTransactionAmount,
  averageOutcomeTransactionAmount,
  startDate: startDateProp,
  endDate: endDateProp,
  vatTotalPayments,
  vatAverage,
  totalCorporateTax,
  totalTaxReturns,
}: YearlySummaryProps) {
  const { t, i18n } = useTranslation();

  const dateRange = useMemo(() => {
    const dateFormat = "dd LLL yyyy";
    const startDate = DateTime.fromISO(startDateProp);
    const endDate = DateTime.fromISO(endDateProp);

    return `${startDate.toFormat(dateFormat, {
      locale: i18n.language,
    })} - ${endDate.toFormat(dateFormat, {
      locale: i18n.language,
    })} (12 months)`;
  }, [i18n.language, endDateProp, startDateProp]);

  return (
    <SimpleGrid columns={[2, null, 3]} spacing="40px">
      <SummaryCard
        title={t("creditReport.incoming")}
        info={dateRange}
        titleIcon={<CashInIcon />}
        titleIconBackground="rgb(0, 198, 132)"
        items={[
          {
            title: monthlyAverageIncome,
            info: t("report.card.monthlyAverage"),
          },
          {
            title: totalIncomeAmount,
            info: t("report.card.total"),
          },
          {
            title: averageRecurringIncome,
            info: t("report.card.recurringIncome"),
          },
          {
            title: incomingTransactionsSize,
            info: t("report.card.transactions"),
          },
          {
            title: averageIncomeTransactionAmount,
            info: t("report.card.averageAmountPerTransaction"),
          },
        ]}
      />
      <SummaryCard
        title={t("creditReport.outgoing")}
        info={dateRange}
        titleIcon={<CashOutIcon />}
        titleIconBackground="rgb(238, 40, 110)"
        items={[
          {
            title: monthlyAverageCost,
            info: t("report.card.monthlyAverage"),
          },
          {
            title: totalOutgoingAmount,
            info: t("report.card.total"),
          },
          {
            title: averageRecurringCosts,
            info: t("report.card.recurringOutgoing"),
          },
          {
            title: outgoingTransactionsSize,
            info: t("report.card.transactions"),
          },
          {
            title: averageOutcomeTransactionAmount,
            info: t("report.card.averageAmountPerTransaction"),
          },
        ]}
      />
      <SummaryCard
        title="Tax"
        info={dateRange}
        titleIcon={
          <Text color="white" margin={0}>
            %
          </Text>
        }
        titleIconBackground="rgb(65, 118, 241)"
        items={[
          { title: vatTotalPayments, info: t("report.card.vatPayments") },
          { title: vatAverage, info: t("report.card.vatAmountAvg") },
          { title: totalCorporateTax, info: t("report.card.corporateTax") },
          { title: totalTaxReturns, info: t("report.card.totalTaxReturns") },
        ]}
      />
    </SimpleGrid>
  );
}
