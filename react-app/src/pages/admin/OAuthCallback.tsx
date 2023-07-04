import { useEffect } from "react";

import { useLocation, useParams } from "react-router-dom";

import { ADMIN_TOKEN_STORAGE_NAME, TOKEN_HEADER_NAME } from "helpers/constants";
import { AdminHTTPClient } from "helpers/HTTPClient";

import Loading from "components/Loading";

import { useRedirect } from "utils/route-utils";

import { useExchangeToken } from "../../api/admin/exchangeToken";

function OAuthCallback() {
  const { search } = useLocation();
  const { providerName: provider } = useParams<{ providerName: string }>();
  const providerName: string = provider as string;

  const redirectToDashboard = useRedirect("/admin/dashboard", true);
  let { data: result } = useExchangeToken(search, providerName);

  useEffect(() => {
    if (!result) {
      return;
    }

    let { access_token } = result;

    if (!access_token || Array.isArray(access_token)) {
      throw new Error("Token is invalid");
    }

    if (process.env.NODE_ENV !== "production") {
      // Set authenticated to true for MSW
      sessionStorage.setItem("isAdminAuthenticated", "true");
    }
    sessionStorage.setItem(ADMIN_TOKEN_STORAGE_NAME, access_token);
    // https://github.com/axios/axios/issues/4193
    // @ts-ignore
    AdminHTTPClient.defaults.headers[TOKEN_HEADER_NAME] = access_token;

    redirectToDashboard();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [result]);

  return <Loading />;
}

export default OAuthCallback;
