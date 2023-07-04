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
  transactionsSize: number;
  accountHolder?: string | null;
  lastDataFetchTime: string;
}

export interface CreditScoreMonthResult {
  month: number;
  year: number;
  period?: string | null;
  isCompleteMonth?: boolean;
  highestBalance: string;
  lowestBalance: string;
  averageBalance?: string;
  totalIncoming: string;
  totalOutgoing: string;
  incomingTransactionsSize: number;
  outgoingTransactionsSize: number;
}

interface CreditScoreDTO {
  userEmail: string;
  adminReport: CreditReport;
  publicKey: string;
  signature: string;
  signatureJsonPaths: string[];
  shouldVerifiedSignature: boolean;
}

export interface CreditScoreResponse extends CreditScoreDTO {
  isSignatureVerified: boolean;
}
