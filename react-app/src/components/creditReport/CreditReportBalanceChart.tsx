import { Chakra } from "@yolt/design-system";
import i18n from "i18n/config";
import { useTranslation } from "react-i18next";
import {
  Area,
  CartesianGrid,
  ComposedChart,
  Legend,
  Line,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import { useBalanceChartData } from "helpers/creditReport";
import { useCurrencyFormatter } from "helpers/formatters";

import type {
  CreditReport,
  CreditScoreMonthResult,
} from "types/admin/creditReport";

const { Box, Stack, Text } = Chakra;

interface CreditReportBalanceChartProps {
  creditReport: CreditReport;
  monthlyReports: CreditScoreMonthResult[];
  isAnimationActive?: boolean;
}

export function CreditReportBalanceChart({
  creditReport,
  monthlyReports,
  isAnimationActive = true,
}: CreditReportBalanceChartProps) {
  const { t } = useTranslation();
  const CurrencyFormatter = useCurrencyFormatter(
    creditReport.currency,
    i18n.language,
    {
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }
  );

  const { balanceChartData, avgAvailable, yAxisMax, yAxisMin } =
    useBalanceChartData(monthlyReports);
  return (
    <Box bg="white" padding="8px" width="100%">
      <Stack>
        <Text as="h1" fontSize="lg" fontWeight="600">
          {t("creditReport.monthBalance")}
        </Text>
        <Text fontSize="sm" fontWeight="200">
          {t("creditReport.lastWholeMonths", {
            months: balanceChartData?.length ?? 0,
          })}
        </Text>
        <ResponsiveContainer width="100%" height={250}>
          <ComposedChart width={400} height={250} data={balanceChartData}>
            <CartesianGrid vertical={false} stroke="#e5e6e0" />
            <XAxis dataKey="month" stroke="#2d2d30" />
            <YAxis
              stroke="#2d2d30"
              width={120}
              axisLine={false}
              tickFormatter={CurrencyFormatter.current.format}
              tickMargin={8}
              domain={[yAxisMin, yAxisMax]}
            />
            <Tooltip isAnimationActive={false} />
            <Legend />
            <Area
              name={t("general.balance")}
              dataKey="balance"
              stroke="#a6aab0"
              fill="#a6aab0"
              activeDot={true}
              isAnimationActive={isAnimationActive}
            />
            {avgAvailable && (
              <Line
                isAnimationActive={isAnimationActive}
                dataKey="averageBalance"
                name={t("general.averageBalance")}
                dot={false}
                activeDot={true}
                stroke="#033072"
                strokeWidth={2}
              />
            )}
          </ComposedChart>
        </ResponsiveContainer>
      </Stack>
    </Box>
  );
}
