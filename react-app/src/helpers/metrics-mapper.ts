import _ from "lodash";
import { Info } from "luxon";
import { MetricsMonthValues, MetricsResponse } from "types/admin/metrics";

import { USER_LANGUAGE_NAME } from "./constants";

export function mapMetrics(
  year: number,
  data: MetricsResponse
): MetricsMonthValues[] {
  const metricsByMonth = _.groupBy(
    _.filter(data, (value) => value.year === year),
    (value) => value.month
  );
  const locale = localStorage.getItem(USER_LANGUAGE_NAME) ?? "en";
  const months = Info.months("short", { locale });
  const result: MetricsMonthValues[] = [];
  _.each([1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12], (i) => {
    let monthValues: any = { month: months[i - 1] };
    monthValues = _.reduce(
      metricsByMonth[i.toString()],
      (result, value) => {
        result[value.status] = value.count;
        return result;
      },
      monthValues
    );
    result.push(monthValues);
  });
  return result;
}
