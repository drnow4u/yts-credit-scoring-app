import { useState } from "react";

import { Button, Chakra } from "@yolt/design-system";
import useSetupLanguage from "i18n/useSetupLanguage";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";

import { useClient } from "api/client";

import PrivacyPolicyModal from "components/modal/PrivacyPolicyModal";
import TermsConditionsModal from "components/modal/TermsConditionsModal";

const { Center, Heading, Stack, Text } = Chakra;

function ConsentRefused() {
  const { t } = useTranslation();
  const [isModalOpen, setIsModalOpen] = useState<
    "termsConditions" | "privacyPolicy" | false
  >(false);

  const navigate = useNavigate();
  const { userHash } = useParams<{ userHash: string }>();

  const { data: client } = useClient();

  useSetupLanguage(client?.language);

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
        <Heading as="h1">Cashflow Analyser</Heading>
        <Heading
          data-testid="subtitle-client-name"
          fontSize="1.3rem"
          fontWeight="300"
        >
          {client?.name}
        </Heading>

        <Text>{t("consent.disagreeMessage")}</Text>
        <Button
          variant="link"
          onClick={() => setIsModalOpen("termsConditions")}
        >
          {t("consent.termsAndConditions")}
        </Button>
        <Button variant="link" onClick={() => setIsModalOpen("privacyPolicy")}>
          {t("consent.privacyPolicy")}
        </Button>

        <Button
          onClick={() => {
            navigate(`/consent/${userHash}`);
          }}
        >
          {t("consent.changeDecision")}
        </Button>
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

export default ConsentRefused;
