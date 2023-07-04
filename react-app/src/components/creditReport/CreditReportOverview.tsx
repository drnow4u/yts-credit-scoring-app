import i18n from "i18n/config";
import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";

import { useCurrencyFormatter } from "helpers/formatters";

import { OverviewBox } from "components/overview/OverviewBox";
import { OverviewItemData } from "components/overview/OverviewItem";

import type { CreditReport } from "types/admin/creditReport";

interface CreditReportOverviewProps {
  creditReport: CreditReport;
  userEmail: string;
}

function CreditReportOverview({
  creditReport,
  userEmail,
}: CreditReportOverviewProps) {
  const { t } = useTranslation();
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
  const initialBalance = CurrencyFormatter.current?.format(
    parseFloat(creditReport.initialBalance)
  );

  const creditLimit = creditReport.creditLimit
    ? CurrencyFormatter.current?.format(parseFloat(creditReport.creditLimit))
    : t("report.overview.creditLimitNotProvided");

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
    {
      title: t("report.overview.initialBalance"),
      info: initialBalance,
    },
  ];
  if (
    creditReport.transactionsSize > 0 &&
    firstTransaction &&
    lastTransaction
  ) {
    items.push({
      title: t("report.overview.period"),
      info: `${firstTransaction.toLocaleString({
        month: "short",
        year: "numeric",
      })} - ${lastTransaction.toLocaleString({
        month: "short",
        year: "numeric",
      })}`,
    });
  }
  items.push(
    {
      title: t("report.overview.contact"),
      info: userEmail,
    },
    {
      title: t("report.overview.creditLimit"),
      info: creditLimit,
    },
    {
      title: t("report.overview.size"),
      info: String(creditReport.transactionsSize),
    }
  );

  return <OverviewBox items={items} />;
}

export default CreditReportOverview;
