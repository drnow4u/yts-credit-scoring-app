import faker from "@faker-js/faker";
import {
  PDGrade,
  PDStatus,
  RiskClassificationResponse,
} from "types/admin/creditReport";

import { pdGradeArray } from "helpers/creditReport";

function mapGradeToRate(grade: PDGrade): [number, number | null] {
  switch (grade) {
    case "A":
      return [0, 0.5];
    case "B":
      return [0.5, 1.5];
    case "C":
      return [1.5, 2.5];
    case "D":
      return [2.5, 3.5];
    case "E":
      return [3.5, 4.5];
    case "F":
      return [4.5, 5.5];
    case "G":
      return [5.5, 6.5];
    case "H":
      return [6.5, 7.5];
    case "I":
      return [7.5, 8.5];
    case "J":
      return [8.5, null];
  }
}

export function createRiskClassification(
  pdStatus: PDStatus | null = "COMPLETED"
): RiskClassificationResponse {
  const grade = faker.random.arrayElement(pdGradeArray);
  const [rateLower, rateUpper] = mapGradeToRate(grade);
  return {
    grade,
    status: pdStatus,
    rateLower,
    rateUpper,
  };
}
