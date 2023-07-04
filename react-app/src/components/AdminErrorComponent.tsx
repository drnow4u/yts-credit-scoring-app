import { useEffect } from "react";

import { Button, Chakra } from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { useTranslation } from "react-i18next";

import Error from "components/Error";

import { useRedirect } from "utils/route-utils";
import { useCleanTokenData } from "utils/storage-utils";
import { isAxiosError } from "utils/type-utils";

import type { FallbackProps } from "react-error-boundary";

const { Text } = Chakra;

function AdminErrorComponent({ error, resetErrorBoundary }: FallbackProps) {
  useSetupLanguage("en");
  const { t } = useTranslation();
  const redirectToLogin = useRedirect("/admin/login", true);
  const cleanAdminToken = useCleanTokenData("admin");

  useEffect(() => {
    if (isAxiosError(error) && error?.response?.status === 401) {
      cleanAdminToken();
      redirectToLogin();
      resetErrorBoundary();
    }
  }, [error, cleanAdminToken, redirectToLogin, resetErrorBoundary]);

  if (isAxiosError(error) && error?.response?.status === 401) {
    return null;
  }

  if (isAxiosError(error) && error?.response?.status === 400) {
    const errorType = error?.response?.data.errorType;

    switch (errorType) {
      case "TOO_MANY_TOKENS":
        return (
          <Error>
            <Text>{t("error.tooManyTokens")}</Text>
            <Button onClick={() => window.location.reload()}>
              {t("button.back")}
            </Button>
          </Error>
        );
    }
  }

  return (
    <Error>
      <Text>{t("error.generic")}</Text>
      <Text>{t("error.code")}:</Text>
      {isAxiosError(error) && error?.response?.data.errorCode && (
        <Text>{JSON.stringify(error?.response?.data.errorCode)}</Text>
      )}
      <Button onClick={() => window.location.reload()}>
        {t("error.reload")}
      </Button>
    </Error>
  );
}

export default AdminErrorComponent;
