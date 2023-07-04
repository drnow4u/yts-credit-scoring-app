import { useEffect } from "react";

import { Chakra } from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { useTranslation } from "react-i18next";

import { useConnectSiteCallback } from "api/site";

import Loading from "components/Loading";

import { useRedirect } from "utils/route-utils";

const { Text } = Chakra;

function SiteConnectCallback() {
  const { t } = useTranslation();
  const { data: connectSiteCallback } = useConnectSiteCallback(
    window.location.href
  );
  const redirectToSelectAccount = useRedirect("/select-account");
  useSetupLanguage();

  useEffect(() => {
    if (connectSiteCallback?.activityId) {
      redirectToSelectAccount();
    } else if (connectSiteCallback?.redirectUrl) {
      window.location.assign(connectSiteCallback?.redirectUrl);
    }
  }, [connectSiteCallback, redirectToSelectAccount]);

  return (
    <Loading>
      <Text>{t("site.connecting")}</Text>
    </Loading>
  );
}

export default SiteConnectCallback;
