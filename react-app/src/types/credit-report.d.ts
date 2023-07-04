interface CreditReportResult {
  month: number;
  year: number;
  isCompleteMonth?: boolean;
  highestBalance: string;
  lowestBalance: string;
  averageBalance: string;
  totalIncoming: string;
  totalOutgoing: string;
  outgoingTransactionsSize: number;
  incomingTransactionsSize: number;
}

export interface CreditReport {
  userId: string;
  iban?: string | null;
  bban?: string | null;
  sortCodeAccountNumber?: string | null;
  maskedPan?: string | null;
  initialBalance: string;
  currency: string;
  newestTransactionDate?: string | null;
  oldestTransactionDate?: string | null;
  creditLimit?: string | null;
  accountHolder?: string | null;
  lastDataFetchTime: string;
}

export interface CreditScoreResponse {
  userEmail: string;
  report: CreditReport;
  additionalTextReport: string | null;
}

export interface ShareReportResponse {
  redirectUrl?: string;
}
