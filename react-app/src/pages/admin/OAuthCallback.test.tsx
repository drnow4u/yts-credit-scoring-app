import { Route, Routes } from "react-router-dom";

import { ADMIN_TOKEN_STORAGE_NAME } from "helpers/constants";

import {
  defaultCustomRenderOptions,
  render,
  waitFor,
} from "test/utils/test-utils";

import OAuthCallback from "./OAuthCallback";

test("handles sign in", async () => {
  const token = "fake_access_token";
  const providerName = "github";

  const initialUrl = `/admin/oauth2/callback/${providerName}?code=code123&state=state456`;
  const { queryResponses } = render(
    <Routes>
      <Route
        path="/admin/oauth2/callback/:providerName"
        element={<OAuthCallback />}
      />
    </Routes>,
    {
      ...defaultCustomRenderOptions,
      initialUrl,
    }
  );

  await waitFor(() =>
    expect(sessionStorage.getItem(ADMIN_TOKEN_STORAGE_NAME)).toBe(
      `Bearer ${token}`
    )
  );
  await waitFor(() =>
    expect(window.location.pathname).toBe("/admin/dashboard")
  );
  const { response } = queryResponses.exchangeToken;

  expect(response).toHaveProperty("access_token");
  expect(response).toHaveProperty("token_type");
  expect(response).toHaveProperty("expires_in");
});
