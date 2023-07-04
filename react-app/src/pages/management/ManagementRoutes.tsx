import { lazy, Suspense } from "react";

import { HeaderBar } from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { Outlet, Route, Routes } from "react-router-dom";

import { HOMEPAGE } from "helpers/constants";

import Loading from "components/Loading";

const Dashboard = lazy(() => import("pages/management/ManagementDashboard"));
const ManagementClientDetails = lazy(
  () => import("pages/management/ManagementClientDetails")
);
const NotFound = lazy(() => import("pages/NotFound"));

const ManagementLayout = () => {
  return (
    <>
      <HeaderBar
        title="Yolt Cashflow Analyser"
        bgColor="yolt.darkBlue"
        logoProps={{
          src: `${HOMEPAGE}YOLT_LOGO-WHITE_GR-RGB.svg`,
          alt: "Yolt logo",
          title: "Yolt Cashflow Analyser",
          to: "/management/",
        }}
      />
      <Outlet />
    </>
  );
};

function ManagementRoutes() {
  useSetupLanguage("en");
  return (
    <Suspense fallback={<Loading />}>
      <Routes>
        <Route path="/" element={<ManagementLayout />}>
          <Route path="/" element={<Dashboard />} />
          <Route
            path="/client/:clientId"
            element={<ManagementClientDetails />}
          />
          {/* Default route for admin subpath */}
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </Suspense>
  );
}

export default ManagementRoutes;
