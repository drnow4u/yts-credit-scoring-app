import faker from "@faker-js/faker";
import { DateTime } from "luxon";

import { FlowStatus } from "helpers/flow-status";

import type {
  FlowStatusString,
  MetricsResponse,
  StatusMonthMetrics,
} from "types/admin/metrics";

export function getMetrics(year?: number): MetricsResponse {
  const months = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];
  let result: MetricsResponse = [];
  for (const month of months) {
    for (const status of Object.values(FlowStatus)) {
      result.push(getSingleMetrics(year ?? DateTime.now().year, month, status));
    }
  }
  return result;
}

export function getSingleMetrics(
  year: number,
  month: number,
  status: FlowStatusString
): StatusMonthMetrics {
  return {
    year: year,
    month: month,
    status: status,
    count: faker.datatype.number({ min: 0, max: 40 }),
  };
}
