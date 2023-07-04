import React, { lazy, Suspense } from "react";

import { ErrorBoundary } from "react-error-boundary";
import { Outlet, Route, Routes } from "react-router-dom";

import AdminErrorComponent from "components/AdminErrorComponent";
import { FeatureToggleProvider } from "components/FeatureToggle.provider";
import Loading from "components/Loading";
import RegisterQueryClientErrorHandler from "components/RegisterQueryClientErrorHandler";

const Dashboard = lazy(() => import("pages/admin/Dashboard"));
const Settings = lazy(() => import("pages/admin/Settings"));
const Login = lazy(() => import("pages/admin/Login"));
const Landing = lazy(() => import("pages/admin/Landing"));
const NotFound = lazy(() => import("pages/NotFound"));
const Header = lazy(() => import("components/Header"));
const Statistics = lazy(() => import("pages/admin/Statistics"));
const CreditReport = lazy(() => import("pages/admin/credit-report"));
const CreditReportPDF = lazy(() => import("pages/admin/credit-report-pdf"));
const OAuthCallback = lazy(() => import("pages/admin/OAuthCallback"));
const PrivacyStatement = lazy(
  () => import("pages/admin/laws/PrivacyStatement")
);
const Legal = lazy(() => import("pages/admin/laws/Legal"));

const AdminLayout = () => {
  return (
    <FeatureToggleProvider>
      <Header />
      <Outlet />
    </FeatureToggleProvider>
  );
};

function AdminRoutes() {
  return (
    <ErrorBoundary FallbackComponent={AdminErrorComponent}>
      <RegisterQueryClientErrorHandler>
        <Suspense fallback={<Loading />}>
          <Routes>
            {/* admin auth route */}
            <Route
              path="oauth2/callback/:providerName"
              element={<OAuthCallback />}
            />
            <Route path="login/*" element={<Login />} />
            <Route path="privacy-statement" element={<PrivacyStatement />} />
            <Route path="legal" element={<Legal />} />
            <Route path="/" element={<Landing />} />
            <Route path="/" element={<AdminLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/settings" element={<Settings />} />
              <Route path="statistics" element={<Statistics />} />
              <Route path="report/:userId" element={<CreditReport />} />
              <Route
                path="report/:userId/download"
                element={<CreditReportPDF />}
              />
              {/* Default route for admin subpath */}
              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </Suspense>
      </RegisterQueryClientErrorHandler>
    </ErrorBoundary>
  );
}

export default AdminRoutes;
