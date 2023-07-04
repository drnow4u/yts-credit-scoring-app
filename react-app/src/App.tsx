import React, { lazy, Suspense } from "react";

import { ErrorBoundary } from "react-error-boundary";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";

import ErrorComponent from "components/ErrorComponent";
import Loading from "components/Loading";
import RegisterQueryClientErrorHandler from "components/RegisterQueryClientErrorHandler";

import { registerClearLocalStorageOnClose } from "./helpers/registerClearLocalStorageOnClose";

const AdminRoutes = lazy(() => import("pages/admin/AdminRoutes"));
const Consent = lazy(() => import("pages/Consent"));
const ConsentRefused = lazy(() => import("pages/ConsentRefused"));
const SelectSite = lazy(() => import("pages/SelectSite"));
const SiteConnectCallback = lazy(() => import("pages/SiteConnectCallback"));
const SelectAccount = lazy(() => import("pages/SelectAccount"));
const CreditReportGenerating = lazy(
  () => import("pages/CreditReportGenerating")
);
const CreditReport = lazy(() => import("pages/CreditReport"));

const NotFound = lazy(() => import("pages/NotFound"));

const ManagementRoutes = lazy(
  () => import("pages/management/ManagementRoutes")
);

registerClearLocalStorageOnClose();

function App() {
  return (
    <Router>
      <ErrorBoundary FallbackComponent={ErrorComponent}>
        <RegisterQueryClientErrorHandler>
          <Suspense fallback={<Loading />}>
            <Routes>
              {/* End user (who will be scored) routes */}
              <Route path="consent/:userHash" element={<Consent />} />
              <Route
                path="consent-refused/:userHash"
                element={<ConsentRefused />}
              />
              <Route path="select-bank" element={<SelectSite />} />
              <Route
                path="site-connect-callback"
                element={<SiteConnectCallback />}
              />
              <Route path="select-account" element={<SelectAccount />} />
              <Route
                path="cashflow-overview"
                element={
                  <Suspense fallback={<CreditReportGenerating />}>
                    <CreditReport />
                  </Suspense>
                }
              />
              {/* Client/admin routes */}
              <Route path="admin/*" element={<AdminRoutes />} />

              {/* management routes */}
              {process.env.REACT_APP_ENABLE_EXPERIMENTAL_MANAGEMENT ===
                "true" && (
                <Route path="management/*" element={<ManagementRoutes />} />
              )}

              {/* Default route */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </Suspense>
        </RegisterQueryClientErrorHandler>
      </ErrorBoundary>
    </Router>
  );
}

export default App;
