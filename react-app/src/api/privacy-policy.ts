import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosQuery } from "helpers/axios-query";

export const QUERY_KEY = "legal-document/privacy-policy";

export const usePrivacyPolicy = () => {
  async function fetchPrivacyPolicy() {
    const { data } = await PublicCreditScoringAPI.getPrivacyPolicy();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchPrivacyPolicy);
};
