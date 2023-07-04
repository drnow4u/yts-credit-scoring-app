import { AxiosError } from "axios";
import translationEN from "i18n/en/translation.json";

import AdminErrorComponent from "components/AdminErrorComponent";

import {
  adminCustomRenderOptions,
  render,
  screen,
  waitFor,
} from "test/utils/test-utils";

const responseErrorMessage = "Broken application";

function generateResponseError(status: number) {
  const error = new AxiosError(
    responseErrorMessage,
    `${status}`,
    {},
    {},
    {
      status: +status,
      data: {},
      statusText: responseErrorMessage,
      headers: {},
      config: {},
    }
  );
  return error;
}
test("renders error", () => {
  const error = new Error(responseErrorMessage);
  const resetErrorBoundary = jest.fn();

  render(
    <AdminErrorComponent
      error={error}
      resetErrorBoundary={resetErrorBoundary}
    />,
    adminCustomRenderOptions
  );

  expect(screen.getByText(translationEN.error.generic)).toBeInTheDocument();
});

test("handles 401 Unauthorized response", async () => {
  const initialUrl = "/admin/error";

  const error = generateResponseError(401);
  const resetErrorBoundary = jest.fn();

  render(
    <AdminErrorComponent
      error={error}
      resetErrorBoundary={resetErrorBoundary}
    />,
    {
      ...adminCustomRenderOptions,
      initialUrl,
    }
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  await waitFor(() => expect(window.location.pathname).toBe("/admin/login"));
});
