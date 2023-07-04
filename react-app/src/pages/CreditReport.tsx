import { useEffect, useMemo, useState } from "react";

import { Button, Chakra } from "@yolt/design-system";
import delay from "lodash/delay";
import { DateTime } from "luxon";
import { useTranslation } from "react-i18next";

import { useCreditReport } from "api/credit-report";
import { useShareReportConfirm, useShareReportRefuse } from "api/share-report";

import { CounterDown } from "components/CounterDown";
import { CreditReportSummary } from "components/creditReport";

const { ButtonGroup, Center, Heading, Stack, Text, useToast } = Chakra;

const SHARE_REPORT_TOAST_ID = "share_report";

enum confirmationStatuses {
  confirmed = "confirmed",
  refused = "refused",
  confirmedRedirect = "confirmedRedirect",
  refusedRedirect = "refusedRedirect",
}

const statusConfirmationText = (status: confirmationStatuses) => {
  const statusText = {
    [confirmationStatuses.confirmed]: "report.confirmed" as const,
    [confirmationStatuses.refused]: "report.refused" as const,
    [confirmationStatuses.confirmedRedirect]:
      "report.confirmedRedirect" as const,
    [confirmationStatuses.refusedRedirect]: "report.refusedRedirect" as const,
  };

  return statusText[status];
};

function CreditReport() {
  const toast = useToast();
  const { t, i18n } = useTranslation();
  const { data: creditReport } = useCreditReport();
  const { mutateAsync: confirmShareReport } = useShareReportConfirm();
  const { mutateAsync: refuseShareReport } = useShareReportRefuse();
  const [buttonEnabled, setButtonEnabled] = useState(true);
  const [confirmationStatus, setConfirmationStatus] = useState<
    confirmationStatuses | false
  >(false);
  const [retrivedDate] = useState(DateTime.now());
  const [isRedirecting, setIsRedirecting] = useState<string>();

  const redirectSecounds = 10;
  const redirectTime = 1000 * redirectSecounds;

  const retrivedOn = useMemo(
    () => retrivedDate.toFormat("dd LLL h.mma", { locale: i18n.language }),
    [i18n.language, retrivedDate]
  );

  useEffect(() => {
    if (!isRedirecting) return;
    const timer = delay(() => {
      window.location.replace(isRedirecting);
    }, redirectTime);

    return () => clearTimeout(timer);
  }, [isRedirecting, redirectTime]);

  if (!creditReport) {
    return null;
  }

  const onRefuseShareReport = async () => {
    try {
      const { data } = await refuseShareReport();
      setButtonEnabled(false);
      const isRedirectResponse = data.redirectUrl && data.redirectUrl !== "";
      const refusedStatus = isRedirectResponse
        ? confirmationStatuses.refusedRedirect
        : confirmationStatuses.refused;

      if (isRedirectResponse) setIsRedirecting(data.redirectUrl);

      setConfirmationStatus(refusedStatus);
    } catch (error) {
      toast({
        title: "Sending report share refusal failed",
        status: "error",
        isClosable: true,
        position: "top",
        id: SHARE_REPORT_TOAST_ID,
      });
    }
  };

  const onConfirmShareReport = async () => {
    try {
      const { data } = await confirmShareReport();
      setButtonEnabled(false);
      const isRedirectResponse = data.redirectUrl && data.redirectUrl !== "";
      const confirmStatus = isRedirectResponse
        ? confirmationStatuses.confirmedRedirect
        : confirmationStatuses.confirmed;

      if (isRedirectResponse) setIsRedirecting(data.redirectUrl);

      setConfirmationStatus(confirmStatus);
    } catch (error) {
      toast({
        title: "Sending report failed",
        status: "error",
        isClosable: true,
        position: "top",
        id: SHARE_REPORT_TOAST_ID,
      });
    }
  };

  return (
    <Center bg="white" as="main" display="flex">
      <Stack
        display="flex"
        spacing="2rem"
        alignItems="center"
        justifyContent="center"
        margin="16px"
        padding="16px"
        overflow="auto"
      >
        <Heading as="h1">{t("report.title")}</Heading>
        <Heading as="h4" fontWeight="400" fontFamily="Gotham SSm, sans-serif">
          {creditReport.additionalTextReport}
        </Heading>
        <Stack
          bg="white"
          boxShadow="0 4px 8px 0 rgba(0,0,0,0.2)"
          borderRadius="5px"
          padding="32px"
        >
          <Stack marginBottom="5" marginRight="auto" width="100%">
            <Stack marginRight="auto">
              <Heading as="h4" fontWeight="400">
                {t("general.details")}
              </Heading>
            </Stack>
          </Stack>
          <CreditReportSummary
            creditReport={creditReport.report}
            userEmail={creditReport.userEmail}
          />
        </Stack>
        {confirmationStatus && (
          <>
            {isRedirecting && <CounterDown countDownMsc={redirectTime} />}
            <Text>{t(statusConfirmationText(confirmationStatus))}</Text>
          </>
        )}
        <Stack marginLeft="auto" marginRight="auto" width="100%">
          <Stack marginLeft="auto" marginRight="auto">
            <Text textColor="yolt.lightGrey" fontWeight="100">
              {t("report.dataRetrievedOn", {
                date: retrivedOn,
              })}
            </Text>
          </Stack>
        </Stack>
        <ButtonGroup spacing="1rem">
          <Button
            onClick={onRefuseShareReport}
            isLoading={!creditReport}
            isDisabled={!buttonEnabled}
            variant="destructive"
          >
            {t("button.cancel")}
          </Button>
          <Button
            onClick={onConfirmShareReport}
            isLoading={!creditReport}
            isDisabled={!buttonEnabled}
            variant="primary"
            type="submit"
          >
            <Text as="span">{t("report.sendOverviewTo")}</Text>
          </Button>
        </ButtonGroup>
      </Stack>
    </Center>
  );
}

export default CreditReport;
