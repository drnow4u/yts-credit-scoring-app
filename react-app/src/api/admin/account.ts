import { getAccount } from "api/admin/creditScoringApi";
import { useAxiosQuery } from "helpers/axios-query";
import isAuthorized from "helpers/isAuthorized";

export const QUERY_KEY = "account";

export const useAccount = () => {
  const userIsAuthorized = isAuthorized();

  async function fetchAccount() {
    const { data } = await getAccount();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchAccount, {
    suspense: false,
    useErrorBoundary: false,
    enabled: userIsAuthorized,
  });
};
