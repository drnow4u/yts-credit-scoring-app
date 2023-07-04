import { useMemo, useState } from "react";

import { Button, Chakra } from "@yolt/design-system";
import dompurify from "dompurify";
import useSetupLanguage from "i18n/useSetupLanguage";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";

import { useClient } from "api/client";
import { useSendConsent } from "api/consent";

import PrivacyPolicyModal from "components/modal/PrivacyPolicyModal";
import TermsConditionsModal from "components/modal/TermsConditionsModal";

import LanguageSelect from "../components/LanguageSelect";

const { Box, ButtonGroup, Center, Heading, Image, Stack, Text, useToast } =
  Chakra;

const CONSENT_TOAST_ID = "consent";

function Consent() {
  const { t } = useTranslation();
  const [isModalOpen, setIsModalOpen] = useState<
    "termsConditions" | "privacyPolicy" | false
  >(false);
  const [consentSelected, setConsentSelected] = useState<"given" | "refused">();
  const toast = useToast();

  const navigate = useNavigate();
  const { userHash } = useParams<{ userHash: string }>();
  const { data: client } = useClient();
  const { mutateAsync: sendConsent, isLoading: isSendingConsent } =
    useSendConsent();

  useSetupLanguage(client?.language);

  const giveConsent = async () => {
    try {
      setConsentSelected("given");
      await sendConsent(true);
      navigate("/select-bank");
    } catch (error) {
      toast({
        title: "Sending consent failed",
        status: "error",
        isClosable: true,
        position: "top",
        id: CONSENT_TOAST_ID,
      });
    }
  };
  const refuseConsent = async () => {
    try {
      setConsentSelected("refused");
      await sendConsent(false);
      navigate(`/consent-refused/${userHash}`);
    } catch (error) {
      toast({
        title: "Sending consent failed",
        status: "error",
        isClosable: true,
        position: "top",
        id: CONSENT_TOAST_ID,
      });
    }
  };

  const additionalText = useMemo(() => {
    if (!client?.additionalTextConsent) return "";

    const sanitizer = dompurify.sanitize;
    return sanitizer(client.additionalTextConsent);
  }, [client?.additionalTextConsent]);

  if (!client) {
    return null;
  }

  return (
    <Center as="main" display="flex" height="100%">
      <Stack
        display="flex"
        spacing="2rem"
        alignItems="center"
        justifyContent="center"
        padding="16px"
        margin="16px"
        bg="white"
      >
        <Heading as="h1">{t("name")}</Heading>
        {client.logo ? (
          <Box data-testid="subtitle-client-logo">
            <Image
              src={`data:image/jpeg;base64,${client.logo}`}
              alt={`${client.name}`}
              maxWidth="100%"
            />
          </Box>
        ) : (
          <Heading
            data-testid="subtitle-client-name"
            fontSize="1.3rem"
            fontWeight="300"
          >
            {client.name}
          </Heading>
        )}

        {client?.additionalTextConsent && (
          <Box paddingTop="1rem" paddingBottom="2rem">
            <Text
              dangerouslySetInnerHTML={{
                __html: additionalText,
              }}
            />
          </Box>
        )}
        <Text>{t("consent.text", { clientName: client.name })}</Text>
        <Button
          variant="link"
          onClick={() => setIsModalOpen("termsConditions")}
        >
          {t("consent.termsAndConditions")}
        </Button>
        <Button variant="link" onClick={() => setIsModalOpen("privacyPolicy")}>
          {t("consent.privacyPolicy")}
        </Button>
        <LanguageSelect />
        <ButtonGroup spacing={5}>
          <Button
            onClick={refuseConsent}
            isLoading={isSendingConsent && consentSelected === "refused"}
            isDisabled={isSendingConsent}
            variant="secondary"
          >
            {t("button.disagree")}
          </Button>
          <Button
            onClick={giveConsent}
            isLoading={isSendingConsent && consentSelected === "given"}
            isDisabled={isSendingConsent}
            variant="primary"
            type="submit"
          >
            <Text as="span">{t("button.agree")}</Text>
          </Button>
        </ButtonGroup>
        <TermsConditionsModal
          isOpen={isModalOpen === "termsConditions"}
          onClose={() => {
            setIsModalOpen(false);
          }}
        />
        <PrivacyPolicyModal
          isOpen={isModalOpen === "privacyPolicy"}
          onClose={() => {
            setIsModalOpen(false);
          }}
        />
      </Stack>
    </Center>
  );
}

export default Consent;
