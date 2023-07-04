import { lazy } from "react";

import { Button, Chakra, ChakraIcons } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import InviteUserModal from "components/modal/InviteUserModal";

const { ButtonGroup, Center, Flex, Heading, Spacer, Stack, useDisclosure } =
  Chakra;
const { AddIcon } = ChakraIcons;

const UsersTable = lazy(() => import("components/UsersTable/UsersTable"));

function Dashboard() {
  const {
    isOpen: isInviteUserModalOpen,
    onOpen: onInviteModalOpen,
    onClose: onInviteModalClose,
  } = useDisclosure();
  const { t } = useTranslation();

  return (
    <Stack as="main" display="flex" padding="2rem" spacing="2rem">
      <Heading textColor="yolt.heroBlue" fontWeight={500} fontSize="lg" as="h1">
        {t("admin.reports")}
      </Heading>
      <Flex>
        <Spacer />
        <Center>
          <ButtonGroup spacing={2}>
            <Button
              size="xs"
              icon={<AddIcon />}
              variant="secondary"
              onClick={onInviteModalOpen}
            >
              {t("admin.addUser")}
            </Button>
          </ButtonGroup>
        </Center>
      </Flex>

      <UsersTable />

      <InviteUserModal
        isOpen={isInviteUserModalOpen}
        onClose={onInviteModalClose}
      />
    </Stack>
  );
}

export default Dashboard;
