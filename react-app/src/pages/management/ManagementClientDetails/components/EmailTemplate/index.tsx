import { useCallback, useState } from "react";

import { Button, Chakra, Select } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { ClientEmailTemplatesDTO } from "types/management/client";

import { useGetClientEmailTemplates } from "api/management/clients";

import Loading from "components/Loading";

import Template from "./Template";

const { Box, Heading, Flex, Text } = Chakra;

enum MODE {
  CREATE = "create",
  EDIT = "edit",
}

export const AdditionalInfo = () => {
  const { t } = useTranslation();
  const { clientId } = useParams<{ clientId: string }>();
  const { data: tamplates } = useGetClientEmailTemplates(clientId);
  const [mode, setMode] = useState<MODE>(MODE.EDIT);
  const [selectedTemplate, setSelectedTemplate] =
    useState<ClientEmailTemplatesDTO>();

  const EmailTemplateEditor = useCallback(() => {
    if (mode === MODE.CREATE) return <Template />;

    return <Template {...selectedTemplate} />;
  }, [selectedTemplate, mode]);

  if (!tamplates) {
    return (
      <Loading>
        <Text>{t(`management.loadingClient`)}</Text>
      </Loading>
    );
  }

  return (
    <Flex
      marginTop={4}
      direction={{ sm: "column", md: "row" }}
      mx="1.5rem"
      w={{ sm: "90%", xl: "95%" }}
      backdropFilter="saturate(200%) blur(50px)"
      backgroundColor="white"
      boxShadow="0px 2px 5.5px rgba(0, 0, 0, 0.02)"
      border="2px solid"
      borderColor="white"
      p="24px"
      borderRadius="20px"
    >
      <Box w="100%">
        <Box m="5">
          <Heading mt="0" as="h4" size="md">
            {t("management.updateEmail.cardTile")}
          </Heading>
          <Box marginY="4" w="100%" display="flex">
            <Box display="inline-block">
              {tamplates.length >= 1 && (
                <Select
                  placeholder={t(
                    "management.updateEmail.chooseTemplateToUpdate"
                  )}
                  onChange={(ev) => {
                    const template = tamplates.find(
                      ({ id }) => id === ev.target.value
                    );
                    setMode(MODE.EDIT);
                    setSelectedTemplate(template);
                  }}
                  name="template"
                >
                  {tamplates.map((template) => (
                    <option key={template.id} value={template.id}>
                      {template.name}
                    </option>
                  ))}
                </Select>
              )}
            </Box>
            <Box display="flex" ml="auto">
              <Button onClick={() => setMode(MODE.CREATE)}>
                {t("management.updateEmail.createNewTemplate")}
              </Button>
            </Box>
          </Box>
          <EmailTemplateEditor />
        </Box>
      </Box>
    </Flex>
  );
};

export default AdditionalInfo;
