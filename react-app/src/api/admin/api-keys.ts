import { DateTime } from "luxon";
import { useQueryClient } from "react-query";
import { ApiKeyDetails } from "types/admin/api-key";

import {
  getApiTokenList,
  getListPermission as getListPermissionApi,
  postCreateApiToken,
  putRevokeApiToken,
} from "api/admin/creditScoringApi";
import { extractPaginationData } from "helpers/api";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";

import { useFeatureToggle } from "components/FeatureToggle.provider";

export const QUERY_PERMISSION_KEY = "API_KEY_PERMISSIONS";
export const QUERY_KEY_TOKEN_LIST = "API_KEY_TOKEN_LIST";
export const CREATE_API_QUERY_KEY = "CREATE_API_KEY_QUERY";

export const useGetApiKeyPermissions = () => {
  async function getListPermission() {
    return await getListPermissionApi();
  }

  return useAxiosQuery(QUERY_PERMISSION_KEY, getListPermission);
};

export const useGetApiKeysList = (page: number = 0, size: number = 10) => {
  const [{ apiToken }] = useFeatureToggle();
  async function getListPermission() {
    const { data, headers } = await getApiTokenList(page, size);
    const paginationData = extractPaginationData(headers);

    const result = data.map((entity) => ({
      ...entity,
      creationDate: DateTime.fromISO(entity.creationDate),
      lastUsed: entity.lastUsed ? DateTime.fromISO(entity.lastUsed) : null,
      expiryDate: entity.lastUsed ? DateTime.fromISO(entity.expiryDate) : null,
    }));

    return { data: result, paginationData };
  }

  return useAxiosQuery([QUERY_KEY_TOKEN_LIST, page, size], getListPermission, {
    keepPreviousData: true,
    enabled: apiToken,
  });
};

export const useCreateApiKey = () => {
  const queryClient = useQueryClient();

  async function createApiKey(apiDetails: ApiKeyDetails) {
    return await postCreateApiToken(apiDetails);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_TOKEN_LIST, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(createApiKey, {
    onSuccess,
  });
};

export const useRevokeApiToken = () => {
  const queryClient = useQueryClient();

  async function revokeApiToken(tokenId: string) {
    return await putRevokeApiToken(tokenId);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_TOKEN_LIST, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(revokeApiToken, {
    onSuccess,
  });
};
