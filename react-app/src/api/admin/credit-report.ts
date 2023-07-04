import {
  getCategorisedCreditReport,
  getCreditReport,
  getCreditReportMonthly,
  getCreditReportOverview,
  getDownloadCreditReport,
  getRiskClassification,
} from "api/admin/creditScoringApi";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";
import {
  categoryTypeFromName,
  CreditScoreTransactionCategory,
} from "helpers/creditReport";

import type { User } from "types/admin/user";

export const QUERY_KEY = "terms-conditions"; //IS it ok?
export const OVERVIEW_QUERY_KEY = "credit-report-overview";
export const MONTHLY_QUERY_KEY = "credit-report-monthly";
export const CATEGORISED_QUERY_KEY = "credit-report-categorised";
export const RISK_QUERY_KEY = "credit-report-risk-classification";

export const useCreditReport = (userId: User["userId"] | undefined) => {
  async function fetchCreditReport() {
    if (!userId) {
      return;
    }

    return await getCreditReport(userId);
  }

  return useAxiosQuery([QUERY_KEY, userId], fetchCreditReport);
};

export const useCreditReportOverview = (
  userId: User["userId"] | undefined,
  toggle: boolean
) => {
  async function fetchCreditReport() {
    if (!userId || !toggle) {
      return;
    }

    return await getCreditReportOverview(userId);
  }

  return useAxiosQuery([OVERVIEW_QUERY_KEY, userId], fetchCreditReport, {
    enabled: toggle,
  });
};

export const useCreditReportMonthly = (
  userId: User["userId"] | undefined,
  toggle: boolean
) => {
  async function fetchCreditReport() {
    if (!userId || !toggle) {
      return;
    }

    return await getCreditReportMonthly(userId);
  }

  return useAxiosQuery([MONTHLY_QUERY_KEY, userId], fetchCreditReport, {
    enabled: toggle,
  });
};

export const useGetDownloadCreditReport = (
  userId: User["userId"] | undefined
) => {
  async function downloadCreditReport() {
    if (!userId) {
      return;
    }

    return await getDownloadCreditReport(userId);
  }

  return useAxiosMutation(downloadCreditReport);
};

export const useCategorisedCreditReport = (
  userId: User["userId"] | undefined,
  toggle: boolean
) => {
  async function fetchCategorisedCreditReport() {
    if (!userId || !toggle) {
      return;
    }

    const { data } = await getCategorisedCreditReport(userId);

    // Add missing categories
    Object.values(CreditScoreTransactionCategory).forEach((value) => {
      if (!data.find((item) => item.categoryName === value)) {
        data.push({
          categoryName: value,
          categoryType: categoryTypeFromName(value),
          totalTransactions: 0,
          averageTransactionAmount: "0",
          totalTransactionAmount: "0",
        });
      }
    });
    return data;
  }

  return useAxiosQuery(
    [CATEGORISED_QUERY_KEY, userId],
    fetchCategorisedCreditReport,
    {
      enabled: toggle,
    }
  );
};

export const useRiskClassification = (
  userId: User["userId"] | undefined,
  toggle: boolean
) => {
  async function fetchRiskClassification() {
    if (!userId || !toggle) {
      return;
    }

    return await getRiskClassification(userId);
  }

  return useAxiosQuery([RISK_QUERY_KEY, userId], fetchRiskClassification, {
    enabled: toggle,
  });
};
