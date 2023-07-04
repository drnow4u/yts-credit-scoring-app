import {
  ApiKeyDetails,
  ApiPermissionsResponse,
  CreateApiKeyResponse,
  GetApiTokensDetailsResponse,
} from "types/admin/api-key";

import { AdminHTTPClient } from "helpers/HTTPClient";

function postCreateApiToken(apiKeyDetails: ApiKeyDetails) {
  return AdminHTTPClient.post<CreateApiKeyResponse>(`token`, apiKeyDetails);
}

function putRevokeApiToken(id: string) {
  return AdminHTTPClient.put<void>(`token/${id}`);
}

function getApiTokenList(page: number, pageSize: number) {
  const params = {
    page: page,
    size: pageSize,
    //TODO: Fixed value for now
    sort: "dateTimeInvited,DESC",
  };
  return AdminHTTPClient.get<GetApiTokensDetailsResponse>(`token`, {
    params,
  });
}

function getListPermission() {
  return AdminHTTPClient.get<ApiPermissionsResponse>(`token/permissions`);
}

export {
  getApiTokenList,
  getListPermission,
  postCreateApiToken,
  putRevokeApiToken,
};
