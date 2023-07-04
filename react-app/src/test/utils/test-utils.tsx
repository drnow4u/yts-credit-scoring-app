import React, { Suspense } from "react";

import { render } from "@testing-library/react";
import { DesignSystemProvider, theme } from "@yolt/design-system";
import { ErrorBoundary, FallbackProps } from "react-error-boundary";
import {
  MutationCache,
  QueryCache,
  QueryClient,
  QueryClientProvider,
} from "react-query";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import {
  ADMIN_TOKEN_STORAGE_NAME,
  USER_TOKEN_STORAGE_NAME,
} from "helpers/constants";

import AdminErrorComponent from "components/AdminErrorComponent";
import ErrorComponent from "components/ErrorComponent";
import Loading from "components/Loading";
import RegisterQueryClientErrorHandler from "components/RegisterQueryClientErrorHandler";

import {
  IS_ADMIN_AUTHENTICATED_KEY,
  IS_USER_AUTHENTICATED_KEY,
} from "test/api-handlers";

import type { RenderOptions } from "@testing-library/react";

import "i18n/config";

interface CustomRenderOptions {
  initialUrl: string;
  errorComponent: React.ComponentType<FallbackProps>;
}

type QueryResponse = {
  key: string;
  response: any;
};

type QueryResponses = {
  [queryKey: string]: QueryResponse;
};

type MutationResponse = {
  requestInput: any;
  response: any;
  url: string;
};

type MutationResponses = {
  [url: string]: MutationResponse;
};

export const defaultCustomRenderOptions: CustomRenderOptions = {
  initialUrl: "/",
  errorComponent: ErrorComponent,
};

export const adminCustomRenderOptions: CustomRenderOptions = {
  initialUrl: "/",
  errorComponent: AdminErrorComponent,
};

const customRender = (
  ui: React.ReactElement,
  customRenderOptions = defaultCustomRenderOptions,
  options?: Omit<RenderOptions, "queries">
) => {
  const mutationResponses = {} as MutationResponses;
  const queryResponses = {} as QueryResponses;

  const queryCache = new QueryCache({
    onSuccess: (response, request) => {
      const { queryKey: keys } = request?.options || {};
      const key = Array.isArray(keys) ? keys[0] : keys;

      if (!key) return;

      queryResponses[key] = {
        response,
        key: key,
      };
    },
  });

  const mutationCache = new MutationCache({
    onSuccess: (request: any) => {
      const { data: requestInput, url } = request?.config || {};
      mutationResponses[url] = {
        requestInput: requestInput ? JSON.parse(requestInput) : {},
        response: request?.data,
        url,
      };
    },
  });

  const queryClient = new QueryClient({
    queryCache,
    mutationCache,
    defaultOptions: {
      queries: {
        retry: 0,
        refetchOnWindowFocus: false,
        suspense: true,
        useErrorBoundary: true,
      },
    },
  });

  const AppProviders = ({ children }: React.PropsWithChildren<{}>) => {
    window.history.replaceState({}, "", customRenderOptions.initialUrl);
    return (
      <DesignSystemProvider theme={theme}>
        <QueryClientProvider client={queryClient}>
          <Router>
            <ErrorBoundary
              FallbackComponent={customRenderOptions.errorComponent}
            >
              <RegisterQueryClientErrorHandler>
                <Suspense fallback={<Loading />}>
                  <Routes>
                    <Route path="*" element={children} />
                  </Routes>
                </Suspense>
              </RegisterQueryClientErrorHandler>
            </ErrorBoundary>
          </Router>
        </QueryClientProvider>
      </DesignSystemProvider>
    );
  };

  return {
    ...render(ui, { wrapper: AppProviders, ...options }),
    mutationResponses,
    queryResponses,
  };
};

export async function loadComponentAuthenticated<P>(path: string) {
  sessionStorage.setItem(IS_USER_AUTHENTICATED_KEY, "true");
  sessionStorage.setItem(IS_ADMIN_AUTHENTICATED_KEY, "true");
  localStorage.setItem(USER_TOKEN_STORAGE_NAME, "Bearer TEST_JWT_TOKEN");
  sessionStorage.setItem(ADMIN_TOKEN_STORAGE_NAME, "Bearer TEST_JWT_TOKEN");

  const { default: Component } = await import(`../../${path}`);

  return Component as React.ComponentType<P>;
}

// re-export everything
export * from "@testing-library/react";

// override render method
export { customRender as render };
