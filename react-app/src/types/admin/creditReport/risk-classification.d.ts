import { pdGradeArray } from "helpers/creditReport";

export type PDStatus = "COMPLETED" | "ERROR" | "ERROR_NOT_ENOUGH_TRANSACTIONS";

export type PDGrade = typeof pdGradeArray[number];

export interface RiskClassificationResponse {
  rateLower: number;
  rateUpper: number | null;
  grade: PDGrade;
  status: PDStatus | null;
}
