import { useParams } from "react-router-dom";

import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosQuery } from "helpers/axios-query";
import {
  TOKEN_HEADER_NAME,
  USER_HASH_STORAGE_NAME,
  USER_TOKEN_STORAGE_NAME,
} from "helpers/constants";
import { PublicHTTPClient } from "helpers/HTTPClient";

import type { TokenResponse } from "types/token";

const QUERY_KEY = "token";

export const useToken = () => {
  const { userHash } = useParams<{ userHash: string }>();
  const storedHash = sessionStorage.getItem(USER_HASH_STORAGE_NAME);
  // To solve issue with web2app flow on mobile https://yolt.atlassian.net/browse/YTSAPP-165
  const storedToken = localStorage.getItem(USER_TOKEN_STORAGE_NAME);

  async function fetchToken() {
    if (!userHash) return undefined;
    const { data } = await PublicCreditScoringAPI.getToken(userHash);
    return data;
  }

  function onSuccess({ token }: TokenResponse) {
    if (!userHash) return;
    sessionStorage.setItem(USER_HASH_STORAGE_NAME, userHash);
    localStorage.setItem(USER_TOKEN_STORAGE_NAME, token);
    // https://github.com/axios/axios/issues/4193
    // @ts-ignore
    PublicHTTPClient.defaults.headers[TOKEN_HEADER_NAME] = token;
  }

  return useAxiosQuery([QUERY_KEY, userHash], fetchToken, {
    enabled: !!userHash,
    onSuccess,
    initialData: () => {
      if (storedHash && storedToken && storedHash === userHash) {
        return { token: storedToken } as TokenResponse;
      }
    },
    staleTime: Infinity,
    refetchOnMount: false,
  });
};
