import { getMetricsData, getMetricsYears } from "api/admin/creditScoringApi";
import { useAxiosQuery } from "helpers/axios-query";
import { mapMetrics } from "helpers/metrics-mapper";

export const QUERY_KEY = "admin/metrics";
export const QUERY_KEY_YEARS = "admin/metrics/years";

export const useMetrics = (year: number) => {
  async function fetchMetrics() {
    const { data } = await getMetricsData(year);
    return mapMetrics(year, data);
  }

  return useAxiosQuery([QUERY_KEY, QUERY_KEY_YEARS, year], fetchMetrics, {
    keepPreviousData: true,
  });
};

export const useYearsAvailableForMetrics = () => {
  async function fetchMetricsYears() {
    const { data } = await getMetricsYears();
    return data;
  }

  return useAxiosQuery([QUERY_KEY_YEARS], fetchMetricsYears, {
    keepPreviousData: true,
  });
};
