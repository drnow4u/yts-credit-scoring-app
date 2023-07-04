import { createContext, FC, useMemo } from "react";

import { useParams } from "react-router";
import {
  CreditScoreMonthResult,
  CreditScoreOverviewResponse,
  CreditScoreResponse,
  RiskClassificationResponse,
} from "types/admin/creditReport";

import {
  useCreditReport,
  useCreditReportMonthly,
  useCreditReportOverview,
  useRiskClassification,
} from "api/admin/credit-report";
import { mapDates } from "helpers/creditReport";

import { useFeatureToggle } from "components/FeatureToggle.provider";

export type ContextState = {
  creditReportData?: CreditScoreResponse | null;
  creditReportOverviewData?: CreditScoreOverviewResponse | null;
  creditReportMonthlyData?: CreditScoreMonthResult[] | undefined | null;
  riskClassification?: RiskClassificationResponse | undefined | null;
  isFetchingOverview: boolean;
  isFetchingMonths: boolean;
  isFetchingCreditReport: boolean;
  isFetchingRisk: boolean;
};

export const CreditReportContext = createContext<ContextState>({
  creditReportData: null,
  creditReportOverviewData: null,
  creditReportMonthlyData: null,
  riskClassification: null,
  isFetchingOverview: false,
  isFetchingMonths: false,
  isFetchingCreditReport: false,
  isFetchingRisk: false,
});

type Props = {};

export const CreditReportProvider: FC<Props> = ({ children }) => {
  const { userId } = useParams<{ userId: string }>();
  const [{ months, overview, pDScoreFeature }] = useFeatureToggle();

  const { data: creditReportData, isFetching: isFetchingCreditReport } =
    useCreditReport(userId);
  const { data: creditReportOverviewData, isFetching: isFetchingOverview } =
    useCreditReportOverview(userId, overview);
  const { data: creditReportMonthlyData, isFetching: isFetchingMonths } =
    useCreditReportMonthly(userId, months);
  const { data: riskClassification, isFetching: isFetchingRisk } =
    useRiskClassification(userId, pDScoreFeature);

  const contextValue = useMemo(
    () => ({
      creditReportData: creditReportData?.data,
      isFetchingCreditReport,
      creditReportOverviewData: creditReportOverviewData?.data,
      isFetchingOverview,
      creditReportMonthlyData:
        creditReportMonthlyData && creditReportData
          ? mapDates(
              creditReportData?.data.adminReport,
              creditReportMonthlyData.data
            ) || []
          : [],
      isFetchingMonths,
      riskClassification: riskClassification?.data,
      isFetchingRisk,
    }),
    [
      creditReportData,
      isFetchingCreditReport,
      creditReportOverviewData,
      isFetchingOverview,
      creditReportMonthlyData,
      isFetchingMonths,
      riskClassification,
      isFetchingRisk,
    ]
  );

  return (
    <CreditReportContext.Provider value={contextValue}>
      {children}
    </CreditReportContext.Provider>
  );
};

export default CreditReportProvider;
