import { useContext } from "react";

import { CreditReportContext } from "./credit-report.provider";

export function useCreditReport() {
  const creditReportDetails = useContext(CreditReportContext);
  return creditReportDetails;
}
