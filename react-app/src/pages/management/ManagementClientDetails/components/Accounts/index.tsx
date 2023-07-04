import { lazy } from "react";

import { Button, Chakra, ChakraIcons } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { ButtonGroup, Center, Flex, Heading, Spacer, Stack, useDisclosure } =
  Chakra;
const { AddIcon } = ChakraIcons;

const UsersTable = lazy(
  () => import("components/ClientsManagement/AccountsTable")
);
const CreateAccountModal = lazy(
  () => import("components/ClientsManagement/CreateAccountModal")
);

function Users() {
  const {
    isOpen: isCreateAccountModalOpen,
    onOpen: onCreateAccountModalOpen,
    onClose: onCreateAccountModalClose,
  } = useDisclosure();
  const { t } = useTranslation();

  return (
    <Stack as="main" display="flex" padding="2rem" spacing="2rem">
      <Heading textColor="yolt.heroBlue" fontWeight={500} fontSize="lg" as="h1">
        {t("management.accountsList")}
      </Heading>
      <Flex>
        <Spacer />
        <Center>
          <ButtonGroup spacing={2}>
            <Button
              size="xs"
              icon={<AddIcon />}
              variant="secondary"
              onClick={onCreateAccountModalOpen}
            >
              {t("management.createAccount")}
            </Button>
          </ButtonGroup>
        </Center>
      </Flex>

      <UsersTable />

      <CreateAccountModal
        isOpen={isCreateAccountModalOpen}
        onClose={onCreateAccountModalClose}
      />
    </Stack>
  );
}

export default Users;
