import { useMemo } from "react";

import { Chakra, CurrencyCell, TableBlock } from "@yolt/design-system";
import i18n from "i18n/config";
import { TFunction, useTranslation } from "react-i18next";

import { useCategorisedCreditReport } from "api/admin/credit-report";
import {
  CreditScoreExpensesCategorisedResponse,
  CreditScoreIncomesCategorisedResponse,
  filterByCategoryType,
  sortExpenses,
  sortIncomes,
} from "helpers/creditReport";
import { useCurrencyFormatter } from "helpers/formatters";

import { useFeatureToggle } from "components/FeatureToggle.provider";

import type { CellProps } from "react-table";

const { Box, Spacer, Stack, Text } = Chakra;

function TranslatedCell<D extends object>({ value }: CellProps<D, string>) {
  const { t } = useTranslation();

  return <>{t(`report.categories.${value}` as any)}</>;
}

const columns = (currency: string, t: TFunction) => [
  {
    accessor: "categoryName",
    Header: t("report.table.category"),
    maxWidth: 80,
    Cell: TranslatedCell,
  },
  {
    accessor: "totalTransactions",
    Header: t("report.table.totalTransactions"),
    maxWidth: 110,
  },
  {
    accessor: "averageTransactionAmount",
    Header: t("report.table.avgTransactionAmount"),
    currency,
    Cell: CurrencyCell,
    maxWidth: 110,
  },
  {
    accessor: "totalTransactionAmount",
    Header: t("report.table.totalTransactionAmount"),
    currency,
    Cell: CurrencyCell,
    maxWidth: 100,
  },
];

interface CreditReportTableProps {
  currency: string;
  userId: string;
}

function CreditReportCategoriesTables({
  currency,
  userId,
}: CreditReportTableProps) {
  const [{ categories }] = useFeatureToggle();
  const { data: categoriesData } = useCategorisedCreditReport(
    userId,
    categories
  );
  const { t } = useTranslation();
  const translatedColumns = columns(currency, t);
  const CurrencyFormatter = useCurrencyFormatter(currency, i18n.language);

  const incoming = useMemo(() => {
    if (categoriesData) {
      return sortIncomes(
        filterByCategoryType<CreditScoreIncomesCategorisedResponse>(
          "INCOMING",
          categoriesData
        )
      );
    }

    return [];
  }, [categoriesData]);

  const totalIncoming = useMemo(() => {
    const total = incoming.reduce<number>(
      (prev, current) =>
        prev + Number.parseFloat(current.totalTransactionAmount),
      0
    );
    return CurrencyFormatter.current?.format(total);
  }, [incoming, CurrencyFormatter]);

  const outgoing = useMemo(() => {
    if (categoriesData) {
      return sortExpenses(
        filterByCategoryType<CreditScoreExpensesCategorisedResponse>(
          "OUTGOING",
          categoriesData
        )
      );
    }

    return [];
  }, [categoriesData]);

  const totalOutgoing = useMemo(() => {
    const total = outgoing.reduce<number>(
      (prev, current) =>
        prev + Number.parseFloat(current.totalTransactionAmount),
      0
    );
    return CurrencyFormatter.current?.format(total);
  }, [outgoing, CurrencyFormatter]);

  return (
    <Box bg="white" padding="8">
      <Stack spacing="8">
        <Stack direction="row" justify="space-between">
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {t("creditReport.incoming")}
          </Text>
          <Spacer />
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {t("report.table.totalAmount")}
          </Text>
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {totalIncoming}
          </Text>
        </Stack>
        <TableBlock<any>
          size="md"
          columns={translatedColumns}
          data={incoming}
          isLoading={!categoriesData}
        />
        <Stack direction="row" justify="space-between">
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {t("creditReport.outgoing")}
          </Text>
          <Spacer />
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {t("report.table.totalAmount")}
          </Text>
          <Text
            as="h2"
            isTruncated
            fontSize="lg"
            textColor="yolt.primaryBlue"
            fontWeight="600"
          >
            {totalOutgoing}
          </Text>
        </Stack>
        <TableBlock<any>
          size="md"
          columns={translatedColumns}
          data={outgoing}
          isLoading={!categoriesData}
        />
      </Stack>
    </Box>
  );
}

export default CreditReportCategoriesTables;
