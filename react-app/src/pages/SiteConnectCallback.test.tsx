import {
  defaultCustomRenderOptions,
  loadComponentAuthenticated,
  render,
  waitFor,
} from "test/utils/test-utils";

let SiteConnectCallback: React.ComponentType;

beforeAll(async () => {
  const ImportedSiteConnectCallback = await loadComponentAuthenticated(
    "pages/SiteConnectCallback"
  );
  SiteConnectCallback = ImportedSiteConnectCallback;
});

test("handles redirection", async () => {
  const initialUrl = "/site-connect-callback?state=abc123&code=abc123";
  const { queryResponses } = render(<SiteConnectCallback />, {
    ...defaultCustomRenderOptions,
    initialUrl,
  });

  await waitFor(() => expect(window.location.pathname).toBe("/select-account"));
  const { response } = queryResponses.banks;

  expect(response).toHaveProperty("activityId");
});
