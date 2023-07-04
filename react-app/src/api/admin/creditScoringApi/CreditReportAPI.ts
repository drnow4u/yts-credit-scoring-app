import {
  CreditScoreCategorisedResponse,
  CreditScoreMonthlyResponse,
  CreditScoreOverviewResponse,
  CreditScoreResponse,
  RiskClassificationResponse,
} from "types/admin/creditReport";
import { InvalidSignature } from "types/admin/invalid-signature";

import { AdminHTTPClient } from "helpers/HTTPClient";

import { verifySignature } from "utils/security";

import type { UserDTO } from "types/admin/user";

async function getCreditReport(userId: UserDTO["userId"]) {
  const { data } = await AdminHTTPClient.get<CreditScoreResponse>(
    `users/${userId}/credit-report`
  );

  if (data.shouldVerifiedSignature) {
    data.isSignatureVerified = await verifySignature(
      data,
      data.signatureJsonPaths
    );
    if (!data.isSignatureVerified) {
      log({
        userId: data.adminReport.userId,
        signature: data.signature,
      });
    }
  }
  return { data };
}

async function getCreditReportOverview(userId: UserDTO["userId"]) {
  const { data } = await AdminHTTPClient.get<CreditScoreOverviewResponse>(
    `users/${userId}/credit-report/overview`
  );
  return { data };
}

async function getCreditReportMonthly(userId: UserDTO["userId"]) {
  const { data } = await AdminHTTPClient.get<CreditScoreMonthlyResponse>(
    `users/${userId}/credit-report/months`
  );

  return { data };
}

function getCategorisedCreditReport(userId: UserDTO["userId"]) {
  return AdminHTTPClient.get<CreditScoreCategorisedResponse[]>(
    `users/${userId}/credit-report/categories`
  );
}

function getRiskClassification(userId: UserDTO["userId"]) {
  return AdminHTTPClient.get<RiskClassificationResponse>(
    `users/${userId}/credit-report/risk-classification`
  );
}

function log(invalidSignature: InvalidSignature) {
  return AdminHTTPClient.post<void>("log/signature", invalidSignature);
}

async function getDownloadCreditReport(userId: UserDTO["userId"]) {
  return AdminHTTPClient.get<BlobPart>(
    `users/${userId}/credit-report/download`,
    {
      responseType: "blob",
    }
  );
}

export {
  getCategorisedCreditReport,
  getCreditReport,
  getCreditReportMonthly,
  getCreditReportOverview,
  getDownloadCreditReport,
  getRiskClassification,
  log,
};
