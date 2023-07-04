import { DateTime } from "luxon";
import { useQueryClient } from "react-query";
import {
  AccountCreateDTO,
  AccountDeleteDTO,
  AccountDetails,
  ClientCreateDTO,
  ClientDetails,
  ClientEmailTemplatesRequest,
  ClientUpdateDTO,
  ClientUpdateFeatureToggle,
} from "types/management/client";
import isUUID from "validator/es/lib/isUUID";

import {
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
} from "api/management/ClientsAPI";
import { extractPaginationData } from "helpers/api";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";

//FIXME: temporary implementation, have to be reviewed when backend will be ready
export const CREATE_CLIENT_QUERY = "CREATE_CLIENT_QUERY";
export const CREATE_ACCOUNT_QUERY = "CREATE_ACCOUNT_QUERY";
export const QUERY_KEY_CLIENT_LIST = "MANAGEMENT_CLIENT_LIST_QUERY";
export const QUERY_KEY_ACCOUNT_LIST = "MANAGEMENT_ACCOUNT_LIST_QUERY";
export const QUERY_KEY_GET_CLIENT = "QUERY_KEY_GET_CLIENt_QUERY";
export const QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES =
  "QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES";
export const QUERY_KEY_POST_CLIENT_EMAIL_TEMPLATES =
  "QUERY_KEY_POST_CLIENT_EMAIL_TEMPLATES";
export const QUERY_KEY_EDIT_CLIENT_EMAIL_TEMPLATES =
  "QUERY_KEY_EDIT_CLIENT_EMAIL_TEMPLATES";

export const useGetClientList = (page: number = 0, size: number = 10) => {
  async function getListClient() {
    const { data, headers } = await getClientList(page, size);
    const paginationData = extractPaginationData(headers);

    for (const entity of data) {
      if (!isUUID(entity.id)) throw new Error("Invalid UUID format");
    }

    const result: ClientDetails[] = data.map((entity) => ({
      ...entity,
      creationDate: DateTime.fromISO(entity.creationDate),
    }));

    return { data: result, paginationData };
  }

  return useAxiosQuery([QUERY_KEY_CLIENT_LIST, page, size], getListClient, {
    keepPreviousData: true,
  });
};

export const useGetAccountsList = (page: number = 0, size: number = 10) => {
  async function getListAccounts() {
    const { data, headers } = await getAccountsList(page, size);
    const paginationData = extractPaginationData(headers);

    for (const entity of data) {
      if (!isUUID(entity.id)) throw new Error("Invalid UUID format");
    }

    const result: AccountDetails[] = data.map((entity) => ({
      ...entity,
      creationDate: DateTime.fromISO(entity.creationDate),
    }));

    return { data: result, paginationData };
  }

  return useAxiosQuery([QUERY_KEY_ACCOUNT_LIST, page, size], getListAccounts, {
    keepPreviousData: true,
  });
};

export const useGetClient = (clientId: ClientDetails["id"] | undefined) => {
  async function fetchClent() {
    if (!clientId) {
      return;
    }

    const { data } = await getClient(clientId);
    return data;
  }

  return useAxiosQuery([QUERY_KEY_GET_CLIENT, clientId], fetchClent);
};

export const useRemoveClient = () => {
  const queryClient = useQueryClient();

  async function removeClient(clientId: ClientDetails["id"]) {
    return await deleteRemoveClient(clientId);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_CLIENT_LIST, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(removeClient, {
    onSuccess,
  });
};

export const useRemoveAccount = () => {
  const queryClient = useQueryClient();

  async function removeAccount(account: AccountDeleteDTO) {
    return await deleteAccount(account);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_ACCOUNT_LIST, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(removeAccount, {
    onSuccess,
  });
};

export function useCreateClient() {
  const queryClient = useQueryClient();

  async function createNewClient(body: ClientCreateDTO) {
    return await createClient(body);
  }

  function onSuccess() {
    queryClient.invalidateQueries(CREATE_CLIENT_QUERY);
  }

  return useAxiosMutation(createNewClient, {
    onSuccess,
    useErrorBoundary: false,
  });
}

export function useCreateAccount() {
  const queryAccount = useQueryClient();

  async function createNewAccount(body: AccountCreateDTO) {
    return await createAccount(body);
  }

  function onSuccess() {
    queryAccount.invalidateQueries(CREATE_ACCOUNT_QUERY);
  }

  return useAxiosMutation(createNewAccount, {
    onSuccess,
    useErrorBoundary: false,
  });
}

export function useUpdateClient() {
  const queryClient = useQueryClient();

  async function createNewClient(body: ClientUpdateDTO) {
    return await updateClient(body);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_GET_CLIENT);
  }

  return useAxiosMutation(createNewClient, {
    onSuccess,
    useErrorBoundary: false,
  });
}

export function useUpdateFeatureToggle() {
  const queryClient = useQueryClient();

  async function createFeatureToggle(body: ClientUpdateFeatureToggle) {
    return await updateFeatureToggle(body);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_GET_CLIENT);
  }

  return useAxiosMutation(createFeatureToggle, {
    onSuccess,
    useErrorBoundary: false,
  });
}

export const useGetClientEmailTemplates = (
  clientId: ClientDetails["id"] | undefined
) => {
  async function fetchTemplates() {
    if (!clientId) {
      return;
    }

    const { data } = await getEmailTemplates(clientId);
    return data;
  }

  return useAxiosQuery(
    [QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES, clientId],
    fetchTemplates
  );
};

export const useCreateClientEmailTemplates = (
  clientId: ClientDetails["id"] | undefined
) => {
  const queryClient = useQueryClient();

  async function createTemplate(body: ClientEmailTemplatesRequest) {
    if (!clientId) {
      return;
    }

    const { data } = await createEmailTemplates(clientId, body);
    return data;
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(createTemplate, {
    onSuccess,
  });
};

export const useEditClientEmailTemplates = (
  clientId: ClientDetails["id"] | undefined
) => {
  const queryClient = useQueryClient();

  async function editTemplate(body: ClientEmailTemplatesRequest) {
    if (!clientId) {
      return;
    }

    const { data } = await editEmailTemplates(clientId, body);
    return data;
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(editTemplate, {
    onSuccess,
  });
};

export const useUploadClientLogo = (clientId: ClientDetails["id"]) => {
  const queryClient = useQueryClient();

  async function editTemplate(body: FormData) {
    const { data } = await uploadLogo(clientId, body);
    return data;
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY_GET_CLIENT_EMAIL_TEMPLATES, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(editTemplate, {
    onSuccess,
  });
};
