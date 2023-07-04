import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosQuery } from "helpers/axios-query";

export const QUERY_KEY = "legal-document/terms-conditions";

export const useTermsConditions = () => {
  async function fetchTermsConditions() {
    const { data } = await PublicCreditScoringAPI.getTermsConditions();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchTermsConditions);
};
