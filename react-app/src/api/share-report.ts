import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosMutation } from "helpers/axios-query";

export const QUERY_KEY = "share-report";

export const useShareReportConfirm = () => {
  async function confirmShareReport() {
    return await PublicCreditScoringAPI.confirmShareReport();
  }

  return useAxiosMutation(confirmShareReport, {
    onSuccess: () => {
      sessionStorage.clear();
      localStorage.clear();
    },
  });
};

export const useShareReportRefuse = () => {
  async function refuseShareReport() {
    return await PublicCreditScoringAPI.refuseShareReport();
  }

  return useAxiosMutation(refuseShareReport, {
    onSuccess: () => {
      sessionStorage.clear();
      localStorage.clear();
    },
  });
};
