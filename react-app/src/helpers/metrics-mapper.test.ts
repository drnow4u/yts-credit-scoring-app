import { MetricsResponse } from "types/admin/metrics";

import { mapMetrics } from "helpers/metrics-mapper";

test("map metrics data for chart input", async () => {
  // GIVEN
  const rawData = [
    {
      year: 2021,
      month: 1,
      status: "INVITED",
      count: 4,
    },
    {
      year: 2021,
      month: 1,
      status: "EXPIRED",
      count: 3,
    },
    {
      year: 2021,
      month: 6,
      status: "INVITED",
      count: 7,
    },
    {
      year: 2020,
      month: 12,
      status: "CONSENT_ACCEPTED",
      count: 1,
    },
  ] as MetricsResponse;

  // WHEN
  const mapped = mapMetrics(2021, rawData);

  // THEN
  expect(mapped.length).toBe(12);
  expect(mapped[0]["month"]).toBe("Jan");
  expect(mapped[5]["month"]).toBe("Jun");
  expect(mapped[11]["month"]).toBe("Dec");
  expect(mapped[0]["INVITED"]).toBe(4);
  expect(mapped[0]["EXPIRED"]).toBe(3);
  expect(mapped[0]["CONSENT_ACCEPTED"]).toBeUndefined();
  expect(mapped[5]["INVITED"]).toBe(7);
  expect(mapped[5]["EXPIRED"]).toBeUndefined();
});
