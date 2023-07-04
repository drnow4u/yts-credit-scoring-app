import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";
import { POLLING_STATUS } from "helpers/constants";

import { useRedirect } from "utils/route-utils";

import type { Account } from "types/account";

const QUERY_KEY = "account";

export const useAccounts = () => {
  async function fetchAccounts() {
    const { data } = await PublicCreditScoringAPI.getAccounts();
    return data;
  }

  return useAxiosQuery(QUERY_KEY, fetchAccounts, {
    /**
     * Polling until the credit report is ready
     * Retry as long as the server returns a 202 status
     */
    retry: (_failureCount, err) => err?.response?.status === POLLING_STATUS,
    retryDelay: 1000,
  });
};

export const useSelectAccount = () => {
  const redirectToCreditReport = useRedirect("/cashflow-overview");

  async function selectAccount(id: Account["id"]) {
    return await PublicCreditScoringAPI.selectAccount(id);
  }

  function onSuccess() {
    redirectToCreditReport();
  }

  return useAxiosMutation(selectAccount, { onSuccess });
};
