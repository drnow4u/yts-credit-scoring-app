import { FC, useEffect, useMemo } from "react";

import {
  AvatarSelect,
  Chakra,
  ChakraIcons,
  HeaderBar,
} from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { ReactComponent as BankAccountIcon } from "icons/bank-account-icon.svg";
import { ReactComponent as IncreaseArrowGraph } from "icons/increase-arrow-graph.svg";
import { ReactComponent as Logout } from "icons/logout.svg";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";

import { useAccount } from "api/admin/account";
import { useLogout } from "api/admin/logout";
import parseFeatureKeys from "helpers/parseFeatureKeys";

import { useFeatureToggle } from "components/FeatureToggle.provider";

import { useRedirect } from "utils/route-utils";

const { Text, Stack } = Chakra;
const { SettingsIcon } = ChakraIcons;

function Header() {
  const { data: account } = useAccount();
  const { mutateAsync: logout } = useLogout();
  const redirectToStatistics = useRedirect("/admin/statistics");
  const redirectToDashboard = useRedirect("/admin/dashboard");
  const redirectToSettings = useRedirect("/admin/settings");
  const location = useLocation();
  const { t } = useTranslation();
  const [{ apiToken }, setContextDetails] = useFeatureToggle();

  useEffect(() => {
    if (account) {
      setContextDetails(parseFeatureKeys(account.clientSettings));
    }
  }, [account, setContextDetails]);
  useSetupLanguage(account?.clientSettings?.defaultLanguage);

  const options = useMemo(() => {
    const list = [
      {
        label:
          location.pathname === "/admin/dashboard"
            ? t("admin.statistics")
            : t("admin.dashboard"),
        icon:
          location.pathname === "/admin/dashboard"
            ? IncreaseArrowGraph
            : BankAccountIcon,
        onClick:
          location.pathname === "/admin/dashboard"
            ? redirectToStatistics
            : redirectToDashboard,
      },
      {
        label: t("button.logout"),
        icon: Logout,
        onClick: logout,
      },
    ];

    if (apiToken) {
      list.unshift({
        label: t("admin.settings"),
        icon: SettingsIcon as FC,
        onClick: redirectToSettings,
      });
    }
    return list;
  }, [
    logout,
    redirectToStatistics,
    redirectToSettings,
    t,
    redirectToDashboard,
    apiToken,
    location.pathname,
  ]);

  return (
    <HeaderBar
      title="Yolt Cashflow Analyser"
      bgColor="yolt.darkBlue"
      logoProps={{
        src: "/YOLT_LOGO-WHITE_GR-RGB.svg",
        alt: "Yolt logo",
        title: "Yolt Cashflow Analyser",
        to: "/admin/dashboard",
      }}
    >
      {account?.email && (
        <Stack direction="row" spacing="4">
          <Text alignSelf="center" m="0" color="white">
            {account?.name}
          </Text>
          <AvatarSelect name={account?.email} options={options} />
        </Stack>
      )}
    </HeaderBar>
  );
}

export default Header;
