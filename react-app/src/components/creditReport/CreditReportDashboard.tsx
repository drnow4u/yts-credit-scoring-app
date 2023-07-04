import { Chakra } from "@yolt/design-system";

import { useCreditReport } from "pages/admin/credit-report/context/useCreditReport";

import { useFeatureToggle } from "components/FeatureToggle.provider";

import { useCurrencyFormatter } from "../../helpers/formatters";
import i18n from "../../i18n/config";
import {
  CreditScoreMonthResult,
  CreditScoreOverviewResponse,
  CreditScoreResponse,
} from "../../types/admin/creditReport";
import { CreditReportBalanceChart } from "./CreditReportBalanceChart";
import { CreditReportInOutChart } from "./CreditReportInOutChart";
import { CreditReportRisk } from "./index";
import { YearlySummary } from "./YearlySummary";

const { Stack, HStack } = Chakra;

interface CreditReportDashboardProps {
  creditScoreResponse: CreditScoreResponse;
  creditScoreOverviewResponse: CreditScoreOverviewResponse;
  creditScoreMonthly: CreditScoreMonthResult[];
  animationChartActive?: boolean;
}

export function CreditReportDashboard({
  creditScoreResponse,
  creditScoreOverviewResponse,
  creditScoreMonthly,
  animationChartActive,
}: CreditReportDashboardProps) {
  const [{ pDScoreFeature }] = useFeatureToggle();
  const { riskClassification } = useCreditReport();
  const CurrencyFormatter = useCurrencyFormatter(
    creditScoreResponse.adminReport.currency,
    i18n.language
  );

  const averageRecurringIncome = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.averageRecurringIncome)
  );

  const averageRecurringCosts = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.averageRecurringCosts)
  );

  const monthlyAverageIncome = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.monthlyAverageIncome)
  );

  const monthlyAverageCost = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.monthlyAverageCost)
  );

  const totalIncomeAmount = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.totalIncomeAmount)
  );

  const totalOutgoingAmount = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.totalOutgoingAmount)
  );

  const averageIncomeTransactionAmount = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.averageIncomeTransactionAmount)
  );

  const averageOutcomeTransactionAmount = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.averageOutcomeTransactionAmount)
  );

  const vatAverage = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.vatAverage)
  );

  const totalTaxReturns = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.totalTaxReturns)
  );

  const totalCorporateTax = CurrencyFormatter.current?.format(
    parseFloat(creditScoreOverviewResponse.totalCorporateTax)
  );

  return (
    <Stack spacing="8" w="100%">
      {pDScoreFeature && riskClassification && (
        <CreditReportRisk riskClassification={riskClassification} />
      )}
      <CreditReportBalanceChart
        monthlyReports={creditScoreMonthly}
        creditReport={creditScoreResponse.adminReport}
        isAnimationActive={animationChartActive}
      />
      <CreditReportInOutChart
        monthlyReports={creditScoreMonthly}
        creditReport={creditScoreResponse.adminReport}
        isAnimationActive={animationChartActive}
      />
      <HStack flexDirection="row" spacing="8" w="100%" mt="5">
        <YearlySummary
          averageRecurringIncome={averageRecurringIncome}
          averageRecurringCosts={averageRecurringCosts}
          monthlyAverageIncome={monthlyAverageIncome}
          monthlyAverageCost={monthlyAverageCost}
          incomingTransactionsSize={
            creditScoreOverviewResponse.incomingTransactionsSize
          }
          outgoingTransactionsSize={
            creditScoreOverviewResponse.outgoingTransactionsSize
          }
          totalIncomeAmount={totalIncomeAmount}
          totalOutgoingAmount={totalOutgoingAmount}
          averageIncomeTransactionAmount={averageIncomeTransactionAmount}
          averageOutcomeTransactionAmount={averageOutcomeTransactionAmount}
          startDate={creditScoreOverviewResponse.startDate}
          endDate={creditScoreOverviewResponse.endDate}
          vatTotalPayments={creditScoreOverviewResponse.vatTotalPayments}
          vatAverage={vatAverage}
          totalCorporateTax={totalCorporateTax}
          totalTaxReturns={totalTaxReturns}
        />
      </HStack>
    </Stack>
  );
}
