import { DateTime } from "luxon";
import { useQueryClient } from "react-query";

import {
  deleteUser as deleteUserApi,
  getUsers,
  inviteUser as inviteUserApi,
  resendUserInvite as resendUserInviteApi,
} from "api/admin/creditScoringApi";
import { extractPaginationData } from "helpers/api";
import { useAxiosMutation, useAxiosQuery } from "helpers/axios-query";
import { USERS_TABLE_POLLING } from "helpers/constants";

import type { User, UserInvite } from "types/admin/user";

export const QUERY_KEY = "admin/users";

export interface UseUsersOptions {
  page: number;
  size: number;
  refetchInterval: number | false;
}

const defaultUseUsersOptions: UseUsersOptions = {
  page: 0,
  size: 10,
  refetchInterval: false,
};

export const useUsers = (options?: Partial<UseUsersOptions>) => {
  const concludedOptions: UseUsersOptions = {
    ...defaultUseUsersOptions,
    ...options,
  };

  concludedOptions.refetchInterval =
    USERS_TABLE_POLLING === false ? false : concludedOptions.refetchInterval;

  async function fetchUsers() {
    const { data, headers } = await getUsers(
      concludedOptions.page,
      concludedOptions.size
    );
    const paginationData = extractPaginationData(headers);
    const result: User[] = data.map((value) => {
      return { ...value, dateInvited: DateTime.fromISO(value.dateInvited) };
    });
    return { result, paginationData };
  }

  return useAxiosQuery(
    [QUERY_KEY, concludedOptions.page, concludedOptions.size],
    fetchUsers,
    {
      keepPreviousData: true,
      refetchOnWindowFocus: concludedOptions.refetchInterval ? true : false,
      refetchInterval: concludedOptions.refetchInterval,
    }
  );
};

export const useDeleteUser = () => {
  const queryClient = useQueryClient();

  async function deleteUser(userId: User["userId"]) {
    return await deleteUserApi(userId);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY, {
      refetchInactive: true,
    });
  }

  return useAxiosMutation(deleteUser, {
    onSuccess,
  });
};

export const useInviteUser = () => {
  const cache = useQueryClient();

  async function inviteUser(userDetails: UserInvite) {
    return await inviteUserApi(userDetails);
  }

  function onSuccess() {
    cache.invalidateQueries(QUERY_KEY);
  }

  return useAxiosMutation(inviteUser, {
    onSuccess,
    useErrorBoundary: false,
  });
};

export const useResendUserInvite = () => {
  const queryClient = useQueryClient();

  async function resendUserInvite(userId: User["userId"]) {
    return await resendUserInviteApi(userId);
  }

  function onSuccess() {
    queryClient.invalidateQueries(QUERY_KEY);
  }

  return useAxiosMutation(resendUserInvite, {
    onSuccess,
  });
};
