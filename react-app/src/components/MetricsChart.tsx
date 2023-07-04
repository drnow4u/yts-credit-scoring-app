import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import {
  Bar,
  BarChart,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import { MetricsMonthValues } from "types/admin/metrics";

import { FlowStatus } from "helpers/flow-status";

const { Stack, Text } = Chakra;

interface MetricsChartProps {
  data: MetricsMonthValues[];
  name: string;
}

//TODO: patterns
const DefaultColors = {
  REPORT_SAVED: "green",
  BANK_ERROR: "darkred",
  EXPIRED: "midnightblue",
  INVITED: "blue",
  BANK_CONSENT_REFUSED: "darkgrey",
  CONSENT_REFUSED: "tomato",
  REPORT_REFUSED: "cornflowerblue",
  CONSENT_ACCEPTED: "goldenrod",
  BANK_CONSENT_ACCEPTED: "teal",
  REPORT_GENERATED: "lightgreen",
};

function MetricsChart({ data, name }: MetricsChartProps) {
  const { t } = useTranslation();
  return (
    <Stack alignItems="center" justifyContent="stretch" spacing="0">
      <Text isTruncated>{name}</Text>
      <ResponsiveContainer width="100%" height={600}>
        <BarChart width={500} height={300} data={data} maxBarSize={50}>
          <YAxis />
          <XAxis dataKey="month" />
          <Tooltip />
          <Legend />
          {Object.values(FlowStatus).map((status) => (
            <Bar
              key={status}
              dataKey={status}
              stackId="a"
              fill={DefaultColors[status] ?? "black"}
              name={t(`metrics.${status}` as any)}
            />
          ))}
        </BarChart>
      </ResponsiveContainer>
    </Stack>
  );
}

export default MetricsChart;
