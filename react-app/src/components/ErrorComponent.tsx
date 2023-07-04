import { useEffect } from "react";

import { Button, Chakra } from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { useTranslation } from "react-i18next";

import Error from "components/Error";

import { useCleanTokenData } from "utils/storage-utils";
import { isAxiosError } from "utils/type-utils";

import type { FallbackProps } from "react-error-boundary";

const { Heading, Text } = Chakra;

/**
 * User error component
 */
function ErrorComponent({ error, resetErrorBoundary }: FallbackProps) {
  const { t } = useTranslation();
  const cleanToken = useCleanTokenData("user");

  useEffect(() => {
    if (
      isAxiosError(error) &&
      (error.response?.status === 401 || error.response?.status === 400)
    ) {
      cleanToken();
    }
  }, [error, cleanToken]);

  useSetupLanguage();

  if (isAxiosError(error) && error?.response?.status === 401) {
    return (
      <Error>
        <Heading as="h1" title="error">
          {t("error.unauthorised")}
        </Heading>
      </Error>
    );
  }

  if (isAxiosError(error) && error?.response?.status === 400) {
    const errorType = error?.response?.data.errorType;

    let message = t("error.unknown");

    switch (errorType) {
      case "FLOW_ENDED":
        message = t("error.flowEnded");
        break;
      case "BANK_CONNECTION_EXIST":
        message = t("error.bankConnectionExist");
        break;
      case "BANK_CONNECTION_FAILURE":
        message = t("error.bankConnectionFailure");
        break;
      case "BANK_CONSENT_REFUSED":
        message = t("error.bankConsentRefused");
        break;
      case "INVITATION_EXPIRED":
        message = t("error.invitationExpired");
        break;
    }

    return (
      <Error>
        <Heading as="h1" title="error">
          {t("error.occurred")}
        </Heading>
        <Text>{message}</Text>
      </Error>
    );
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

export default ErrorComponent;
