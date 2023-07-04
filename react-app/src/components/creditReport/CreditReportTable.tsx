import { useMemo } from "react";

import { Chakra, CurrencyCell, TableBlock } from "@yolt/design-system";
import { TFunction, useTranslation } from "react-i18next";

import type {
  CreditReport,
  CreditScoreMonthResult,
} from "types/admin/creditReport";

const { Box } = Chakra;

const columns = (t: TFunction, currency: string) => [
  {
    accessor: "period",
    Header: t("report.table.period") ?? "",
    maxWidth: 80,
  },
  {
    accessor: "highestBalance",
    Header: t("report.table.highestBalance") ?? "",
    currency,
    Cell: CurrencyCell,
    maxWidth: 110,
  },
  {
    accessor: "lowestBalance",
    Header: t("report.table.lowestBalance") ?? "",
    currency,
    Cell: CurrencyCell,
    maxWidth: 110,
  },
  {
    accessor: "totalIncoming",
    Header: t("report.table.totalIncoming") ?? "",
    currency,
    Cell: CurrencyCell,
    maxWidth: 100,
  },
  {
    accessor: "incomingTransactionsSize",
    Header: t("report.table.sizeIncoming") ?? "",
  },
  {
    accessor: "totalOutgoing",
    Header: t("report.table.totalOutgoing") ?? "",
    currency,
    Cell: CurrencyCell,
    maxWidth: 100,
  },
  {
    accessor: "outgoingTransactionsSize",
    Header: t("report.table.sizeOutgoing") ?? "",
  },
];

interface CreditReportTableProps {
  creditReport: CreditReport;
  creditScoreMonthly: CreditScoreMonthResult[];
}

function CreditReportTable({
  creditReport,
  creditScoreMonthly,
}: CreditReportTableProps) {
  const { t } = useTranslation();

  const translatedColumns = columns(t, creditReport.currency);

  const creditScoreMonthlySortedDesc = useMemo(
    () =>
      (creditScoreMonthly && creditScoreMonthly
        ? [...creditScoreMonthly]
        : undefined
      )?.sort(function (creditReport1, creditReport2) {
        return (
          new Date(creditReport2.year, creditReport2.month).valueOf() -
          new Date(creditReport1.year, creditReport1.month).valueOf()
        );
      }),
    [creditScoreMonthly]
  );

  return (
    <Box bg="white">
      <TableBlock<any>
        size="md"
        columns={translatedColumns}
        data={creditScoreMonthlySortedDesc ?? []}
        isLoading={!creditReport}
      />
    </Box>
  );
}

export default CreditReportTable;
