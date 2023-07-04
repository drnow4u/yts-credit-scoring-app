type CreditScoreMonthlyDTO = {
  month: number;
  year: number;
  highestBalance: string;
  lowestBalance: string;
  averageBalance?: string;
  totalIncoming: string;
  totalOutgoing: string;
  incomingTransactionsSize: number;
  outgoingTransactionsSize: number;
};

export type CreditScoreMonthlyResponse = {
  monthlyReports: CreditScoreMonthlyDTO[];
};
