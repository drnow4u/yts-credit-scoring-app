import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";

import { useCurrencyFormatter } from "helpers/formatters";

import { OverviewBox } from "components/overview/OverviewBox";
import { OverviewItemData } from "components/overview/OverviewItem";

import type { CreditReport } from "types/credit-report";

interface CreditReportSummaryProps {
  creditReport: CreditReport;
  userEmail: string;
}

function CreditReportSummary({
  creditReport,
  userEmail,
}: CreditReportSummaryProps) {
  const { t, i18n } = useTranslation();
  const CurrencyFormatter = useCurrencyFormatter(
    creditReport.currency,
    i18n.language
  );

  const firstTransaction = creditReport.oldestTransactionDate
    ? DateTime.fromISO(creditReport.oldestTransactionDate)
    : null;
  const lastTransaction = creditReport.newestTransactionDate
    ? DateTime.fromISO(creditReport.newestTransactionDate)
    : null;

  const initialBalance = CurrencyFormatter.current?.format(
    parseFloat(creditReport.initialBalance)
  );

  const accountNumber =
    creditReport.iban ??
    creditReport.sortCodeAccountNumber ??
    creditReport.bban ??
    creditReport.maskedPan ??
    "";
  const accountHolder =
    creditReport.accountHolder ??
    t("report.overview.accountHolderNotProvided") ??
    "";
  const retrievalDate = DateTime.fromISO(
    creditReport.lastDataFetchTime
  ).toLocaleString(DateTime.DATE_FULL);

  const items: OverviewItemData[] = [
    {
      title: t("report.overview.submitted"),
      info: retrievalDate,
    },
    {
      title: t("report.overview.accountHolder"),
      info: accountHolder,
    },
    {
      title: t("report.overview.accountNumber"),
      info: accountNumber,
    },
  ];

  if (firstTransaction && lastTransaction) {
    items.push({
      title: t("report.overview.period"),
      info: `${firstTransaction.toLocaleString(
        {
          month: "short",
          year: "numeric",
        },
        {
          locale: i18n.language,
        }
      )} - ${lastTransaction.toLocaleString(
        {
          month: "short",
          year: "numeric",
        },
        {
          locale: i18n.language,
        }
      )}`,
    });
  }

  items.push(
    {
      title: t("report.overview.contact"),
      info: userEmail,
    },
    {
      title: t("report.overview.initialBalance"),
      info: initialBalance,
    }
  );

  return <OverviewBox fit items={items} />;
}

export default CreditReportSummary;
