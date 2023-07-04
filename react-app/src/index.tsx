import { StrictMode } from "react";

import { DesignSystemProvider, theme } from "@yolt/design-system";
import ReactDOM from "react-dom";
import { ErrorBoundary } from "react-error-boundary";
import { QueryClient, QueryClientProvider } from "react-query";

import { HOMEPAGE } from "helpers/constants";

import ErrorComponent from "components/ErrorComponent";

import App from "./App";

import type { SetupWorkerApi } from "msw/lib/types";

import "@csstools/normalize.css";
import "i18n/config";
import "./app.scss";

const appElementId = "root";

if (
  process.env.NODE_ENV === "development" &&
  process.env.REACT_APP_DISABLE_MSW !== "true"
) {
  const { worker } = require("./test/browser");
  (worker as SetupWorkerApi).start({
    serviceWorker: {
      url: `${HOMEPAGE}mockServiceWorker.js`,
    },
  });
}

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 0,
      refetchOnWindowFocus: false,
      suspense: true,
      useErrorBoundary: true,
    },
    mutations: {
      useErrorBoundary: true,
    },
  },
});

ReactDOM.render(
  <StrictMode>
    <DesignSystemProvider theme={theme}>
      <ErrorBoundary FallbackComponent={ErrorComponent}>
        <QueryClientProvider client={queryClient}>
          <App />
        </QueryClientProvider>
      </ErrorBoundary>
    </DesignSystemProvider>
  </StrictMode>,
  document.getElementById(appElementId)
);
