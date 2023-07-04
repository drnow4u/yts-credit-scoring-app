interface CreditScoreOverviewDTO {
  averageRecurringIncome: string;
  averageRecurringCosts: string;
  startDate: string;
  endDate: string;
  incomingTransactionsSize: number;
  outgoingTransactionsSize: number;
  monthlyAverageIncome: string;
  monthlyAverageCost: string;
  totalIncomeAmount: string;
  totalOutgoingAmount: string;
  averageIncomeTransactionAmount: string;
  averageOutcomeTransactionAmount: string;
  vatTotalPayments: number;
  vatAverage: string;
  totalCorporateTax: string;
  totalTaxReturns: string;
}

export interface CreditScoreOverviewResponse extends CreditScoreOverviewDTO {
  isSignatureVerified?: boolean;
}
