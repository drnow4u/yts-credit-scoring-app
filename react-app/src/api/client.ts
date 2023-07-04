import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useToken } from "api/token";
import { useAxiosQuery } from "helpers/axios-query";

const QUERY_KEY = "client";

export const useClient = () => {
  const { data: token } = useToken();

  async function fetchClient() {
    const { data } = await PublicCreditScoringAPI.getClient();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchClient, { enabled: !!token });
};
