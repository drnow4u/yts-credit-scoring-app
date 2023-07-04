import { AdminHTTPClient } from "helpers/HTTPClient";

import type { UserDTO, UserInvite } from "types/admin/user";

function getUsers(page: number, pageSize: number) {
  const params = {
    page: page,
    size: pageSize,
    //TODO: Fixed value for now
    sort: "dateTimeInvited,DESC",
  };
  return AdminHTTPClient.get<UserDTO[]>("users", { params });
}

function deleteUser(userId: UserDTO["userId"]) {
  return AdminHTTPClient.delete<void>(`users/${userId}`);
}

function inviteUser(userDetails: UserInvite) {
  return AdminHTTPClient.post<void>(`users/invite`, userDetails);
}

function resendUserInvite(userId: UserDTO["userId"]) {
  return AdminHTTPClient.put<void>(`users/${userId}/resend-invite`);
}

export { deleteUser, getUsers, inviteUser, resendUserInvite };
