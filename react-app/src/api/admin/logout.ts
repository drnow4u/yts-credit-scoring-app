import { useAxiosMutation } from "helpers/axios-query";
import { TOKEN_HEADER_NAME } from "helpers/constants";
import { AdminHTTPClient } from "helpers/HTTPClient";

import { useRedirect } from "utils/route-utils";
import { useCleanTokenData } from "utils/storage-utils";

import { logout as apiLogout } from "./creditScoringApi";

export const useLogout = () => {
  const redirectToLogin = useRedirect("/admin/login", true);
  const cleanAdminToken = useCleanTokenData("admin");

  async function logout() {
    return await apiLogout();
  }

  function onSuccess() {
    cleanAdminToken();
    // https://github.com/axios/axios/issues/4193
    // @ts-ignore
    AdminHTTPClient.defaults.headers[TOKEN_HEADER_NAME] = undefined;
    redirectToLogin();
  }

  return useAxiosMutation(logout, {
    onSuccess,
  });
};
