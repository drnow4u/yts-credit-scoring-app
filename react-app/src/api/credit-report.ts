import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosQuery } from "helpers/axios-query";
import { POLLING_STATUS } from "helpers/constants";

export const QUERY_KEY = "credit-report";

export const useCreditReport = () => {
  async function fetchCreditReport() {
    const { data } = await PublicCreditScoringAPI.getCreditReport();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchCreditReport, {
    /**
     * Polling until the credit report is ready
     * Retry as long as the server returns a 202 status
     */
    retry: (_failureCount, err) => err?.response?.status === POLLING_STATUS,
    retryDelay: 1000,
  });
};
