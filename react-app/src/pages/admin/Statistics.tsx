import { useState } from "react";

import { Chakra, Select } from "@yolt/design-system";
import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";

import { useMetrics, useYearsAvailableForMetrics } from "api/admin/metrics";

import MetricsChart from "components/MetricsChart";

const { Box, Center, Heading, Stack } = Chakra;

function Statistics() {
  const [selectedYear, setSelectedYear] = useState<number>(DateTime.now().year);
  const { data: availableYears } = useYearsAvailableForMetrics();
  const { data: metrics } = useMetrics(selectedYear);
  const { t } = useTranslation();

  return (
    <Stack as="main" display="flex" padding="2rem" spacing="2rem">
      <Stack display="flex" direction="row" spacing="2rem">
        <Center>
          <Heading as="h1">{t("admin.statistics")}</Heading>
        </Center>
        <Center>
          <Select
            id="selectedYear"
            value={selectedYear}
            onChange={(e) => {
              setSelectedYear(Number(e.target.value));
            }}
          >
            {(availableYears ?? [selectedYear]).map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </Select>
        </Center>
      </Stack>
      <Box bg="white">
        <MetricsChart
          name={`Statistics ${selectedYear}`}
          data={metrics ?? []}
        />
      </Box>
    </Stack>
  );
}

export default Statistics;
