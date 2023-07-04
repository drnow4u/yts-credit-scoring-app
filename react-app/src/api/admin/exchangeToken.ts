import { getExchangeToken } from "api/admin/creditScoringApi/AdminCreditScoringAPI";
import { useAxiosQuery } from "helpers/axios-query";

export const QUERY_KEY = "exchangeToken";

export const useExchangeToken = (url: string, providerName: string) => {
  async function fetchExchangeToken() {
    const { data } = await getExchangeToken(url, providerName);
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchExchangeToken, {
    suspense: false,
    useErrorBoundary: false,
  });
};
