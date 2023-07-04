import { lazy } from "react";

import { Button, Chakra, ChakraIcons } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { ButtonGroup, Center, Flex, Heading, Spacer, Stack, useDisclosure } =
  Chakra;
const { AddIcon } = ChakraIcons;

const ClientsTable = lazy(
  () => import("components/ClientsManagement/ClientsTable")
);
const CreateClientModal = lazy(
  () => import("components/ClientsManagement/CreateClientModal")
);

function Dashboard() {
  const {
    isOpen: isCreateClientModalOpen,
    onOpen: onCreateClientModalOpen,
    onClose: onCreateClientModalClose,
  } = useDisclosure();
  const { t } = useTranslation();

  return (
    <Stack as="main" display="flex" padding="2rem" spacing="2rem">
      <Heading textColor="yolt.heroBlue" fontWeight={500} fontSize="lg" as="h1">
        {t("management.clients")}
      </Heading>
      <Flex>
        <Spacer />
        <Center>
          <ButtonGroup spacing={2}>
            <Button
              size="xs"
              icon={<AddIcon />}
              variant="secondary"
              onClick={onCreateClientModalOpen}
            >
              {t("management.createClient")}
            </Button>
          </ButtonGroup>
        </Center>
      </Flex>

      <ClientsTable />

      <CreateClientModal
        isOpen={isCreateClientModalOpen}
        onClose={onCreateClientModalClose}
      />
    </Stack>
  );
}

export default Dashboard;
