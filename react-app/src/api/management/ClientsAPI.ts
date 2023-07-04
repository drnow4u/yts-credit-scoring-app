import {
  AccountCreateDTO,
  AccountDeleteDTO,
  AccountsDetailsResponsee,
  ClientCreatedResponse,
  ClientCreateDTO,
  ClientDetails,
  ClientEmailTemplatesDTO,
  ClientEmailTemplatesRequest,
  ClientsDetailsResponse,
  ClientUpdateDTO,
  ClientUpdateFeatureToggle,
} from "types/management/client";

import { ManagementHTTPClient } from "helpers/HTTPClient";

//FIXME: temporary implementation, have to be reviewed when backend will be ready

function deleteRemoveClient(id: ClientDetails["id"]) {
  return ManagementHTTPClient.delete<void>(`clients/${id}`);
}

async function createClient(body: ClientCreateDTO) {
  return ManagementHTTPClient.post<ClientCreatedResponse>(`client`, body);
}

async function createAccount(body: AccountCreateDTO) {
  return ManagementHTTPClient.post<ClientCreatedResponse>(`account`, body);
}

async function getClient(id: ClientDetails["id"]) {
  return ManagementHTTPClient.get<ClientCreatedResponse>(`client/${id}`);
}

async function getEmailTemplates(id: ClientDetails["id"]) {
  return ManagementHTTPClient.get<ClientEmailTemplatesDTO[]>(
    `client/${id}/email-templates`
  );
}

async function createEmailTemplates(
  id: ClientDetails["id"],
  body: ClientEmailTemplatesRequest
) {
  return ManagementHTTPClient.post<ClientEmailTemplatesDTO>(
    `client/${id}/email-template`,
    body
  );
}

async function editEmailTemplates(
  id: ClientDetails["id"],
  body: ClientEmailTemplatesRequest
) {
  return ManagementHTTPClient.put<ClientEmailTemplatesRequest>(
    `client/${id}/email-template`,
    body
  );
}

async function uploadLogo(id: ClientDetails["id"], body: FormData) {
  return ManagementHTTPClient.post<FormData>(`client/${id}/logo`, body, {
    headers: {
      "Content-Type": "multipart/form-data",
    },
  });
}

function getClientList(page: number, pageSize: number) {
  const params = {
    page: page,
    size: pageSize,
    //TODO: Fixed value for now
    sort: "dateTimeInvited,DESC",
  };
  return ManagementHTTPClient.get<ClientsDetailsResponse>(`clients`, {
    params,
  });
}

function getAccountsList(page: number, pageSize: number) {
  const params = {
    page: page,
    size: pageSize,
    //TODO: Fixed value for now
    sort: "dateTimeInvited,DESC",
  };
  return ManagementHTTPClient.get<AccountsDetailsResponsee>(`accounts`, {
    params,
  });
}

function deleteAccount({ id, ...body }: AccountDeleteDTO) {
  return ManagementHTTPClient.delete<void>(`account/${id}`);
}

async function updateClient(body: ClientUpdateDTO) {
  return ManagementHTTPClient.put<ClientCreatedResponse>(`client/`, body);
}

async function updateFeatureToggle(body: ClientUpdateFeatureToggle) {
  return ManagementHTTPClient.put<ClientCreatedResponse>(`client/toggle`, body);
}

export {
  createAccount,
  createClient,
  createEmailTemplates,
  deleteAccount,
  deleteRemoveClient,
  editEmailTemplates,
  getAccountsList,
  getClient,
  getClientList,
  getEmailTemplates,
  updateClient,
  updateFeatureToggle,
  uploadLogo,
};
