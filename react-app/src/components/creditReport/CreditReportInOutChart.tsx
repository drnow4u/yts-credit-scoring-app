import { Chakra } from "@yolt/design-system";
import i18n from "i18n/config";
import { useTranslation } from "react-i18next";
import {
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import { useInOutChartData } from "helpers/creditReport";
import { useCurrencyFormatter } from "helpers/formatters";

import type {
  CreditReport,
  CreditScoreMonthResult,
} from "types/admin/creditReport";

const { Box, Stack, Text } = Chakra;

interface CreditReportInOutChartProps {
  creditReport: CreditReport;
  monthlyReports: CreditScoreMonthResult[];
  isAnimationActive?: boolean;
}

export function CreditReportInOutChart({
  creditReport,
  monthlyReports,
  isAnimationActive = true,
}: CreditReportInOutChartProps) {
  const { t } = useTranslation();
  const CurrencyFormatter = useCurrencyFormatter(
    creditReport.currency,
    i18n.language,
    {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }
  );

  const inOutChartData = useInOutChartData(monthlyReports);

  return (
    <Box bg="white" padding="8px" width="100%">
      <Stack>
        <Text as="h1" fontSize="lg" fontWeight="600">
          {t("report.incomingAndOutgoingPerMonth")}
        </Text>
        <Text fontSize="sm" fontWeight="200">
          {t("creditReport.lastWholeMonths", {
            months: inOutChartData?.length ?? 0,
          })}
        </Text>
        <ResponsiveContainer width="100%" height={250}>
          <LineChart width={400} height={250} data={inOutChartData}>
            <CartesianGrid vertical={false} stroke="#e5e6e0" />
            <XAxis dataKey="month" stroke="#2d2d30" />
            <YAxis
              stroke="#2d2d30"
              width={120}
              axisLine={false}
              tickFormatter={CurrencyFormatter.current.format}
              tickMargin={8}
              type="number"
            />
            <Tooltip isAnimationActive={isAnimationActive} />
            <Legend />
            <Line
              isAnimationActive={isAnimationActive}
              name={t("report.table.totalIncoming")}
              dot={false}
              dataKey={(v) => parseFloat(v.totalIncoming)}
              activeDot={true}
              stroke="#00f3a6"
              strokeWidth={2}
            />
            <Line
              isAnimationActive={isAnimationActive}
              dataKey={(v) => parseFloat(v.totalOutgoing)}
              name={t("report.table.totalOutgoing")}
              dot={false}
              activeDot={true}
              stroke="#f72585"
              strokeWidth={2}
            />
          </LineChart>
        </ResponsiveContainer>
      </Stack>
    </Box>
  );
}
