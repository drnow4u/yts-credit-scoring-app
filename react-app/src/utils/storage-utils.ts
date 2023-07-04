import { useCallback } from "react";

import { useQueryClient } from "react-query";

import {
  ADMIN_TOKEN_STORAGE_NAME,
  USER_TOKEN_STORAGE_NAME,
} from "helpers/constants";

type TokenStorage = "admin" | "user";
const tokenStorageOptions: Record<TokenStorage, string> = {
  admin: ADMIN_TOKEN_STORAGE_NAME,
  user: USER_TOKEN_STORAGE_NAME,
};

export const useCleanTokenData = (tokenStorage: TokenStorage) => {
  const queryClient = useQueryClient();

  return useCallback(() => {
    queryClient.clear();
    const tokenName = tokenStorageOptions[tokenStorage];
    sessionStorage.removeItem(tokenName);
    localStorage.removeItem(tokenName);
  }, [tokenStorage, queryClient]);
};
