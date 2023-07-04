import { FlowStatus } from "helpers/flow-status";

export type FlowStatusString = keyof typeof FlowStatus;

export interface StatusMonthMetrics {
  year: number;
  month: number;
  status: FlowStatusString;
  count: number;
}

export type MetricsResponse = StatusMonthMetrics[];

export type MetricsYearsResponse = number[];

export type MetricsMonthValues = {
  [index: string]: number;
};
