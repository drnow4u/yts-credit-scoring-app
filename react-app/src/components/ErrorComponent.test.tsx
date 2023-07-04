import { AxiosError } from "axios";
import translationEN from "i18n/en/translation.json";

import { USER_TOKEN_STORAGE_NAME } from "helpers/constants";

import ErrorComponent from "components/ErrorComponent";

import { render, screen } from "test/utils/test-utils";

const responseErrorMessage = "Broken application";

type AxiosErrorTypeStatus = {
  status: number;
  data?: {
    errorCode?: string;
    errorType?: string;
  };
};

function generateResponseError(message: string, status: AxiosErrorTypeStatus) {
  const error = new AxiosError(
    message,
    `${status}`,
    {},
    {},
    {
      status: status.status,
      data: status.data,
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
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.getByText(translationEN.error.generic)).toBeInTheDocument();
});

test("handles 401 Unauthorized response", async () => {
  const error = generateResponseError(responseErrorMessage, { status: 401 });
  const resetErrorBoundary = jest.fn();
  localStorage.setItem(USER_TOKEN_STORAGE_NAME, "Bearer TEST_JWT_TOKEN");

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  expect(
    screen.getByText(translationEN.error.unauthorised)
  ).toBeInTheDocument();
  expect(localStorage.getItem(USER_TOKEN_STORAGE_NAME)).toBeNull();
});

test("handles 400 Bad Request - FLOW_ENDED", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "FLOW_ENDED",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(screen.getByText(translationEN.error.flowEnded)).toBeInTheDocument();
});

test("handles 400 Bad Request - BANK_CONNECTION_EXIST", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "BANK_CONNECTION_EXIST",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(
    screen.getByText(translationEN.error.bankConnectionExist)
  ).toBeInTheDocument();
});

test("handles 400 Bad Request - BANK_CONNECTION_FAILURE", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "BANK_CONNECTION_FAILURE",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(
    screen.getByText(translationEN.error.bankConnectionFailure)
  ).toBeInTheDocument();
});

test("handles 400 Bad Request - BANK_CONSENT_REFUSED", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "BANK_CONSENT_REFUSED",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(
    screen.getByText(translationEN.error.bankConsentRefused)
  ).toBeInTheDocument();
});

test("handles 400 Bad Request - INVITATION_EXPIRED", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "INVITATION_EXPIRED",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(
    screen.getByText(translationEN.error.invitationExpired)
  ).toBeInTheDocument();
});

test("handles 400 Bad Request - UNKNOWN", async () => {
  const error = generateResponseError("responseErrorMessage", {
    status: 400,
    data: {
      errorCode: "abba6ef8-06d6-462e-8b03-9a14579dd360",
      errorType: "SOME_NOT_EXISTING_TYPE",
    },
  });
  const resetErrorBoundary = jest.fn();

  render(
    <ErrorComponent error={error} resetErrorBoundary={resetErrorBoundary} />
  );

  expect(screen.queryByText(responseErrorMessage)).not.toBeInTheDocument();
  const htmlElement = screen.getByTitle(/error/i);
  expect(htmlElement).toBeInTheDocument();
  expect(screen.getByText(translationEN.error.unknown)).toBeInTheDocument();
});
