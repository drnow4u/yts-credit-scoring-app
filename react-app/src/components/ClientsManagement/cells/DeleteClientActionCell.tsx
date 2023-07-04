import { Button, Chakra, ChakraIcons, Modal } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import { useRemoveClient } from "api/management/clients";
import useThrottle from "helpers/useThrottle";

import type { CellProps } from "react-table";

const { useDisclosure } = Chakra;

const { DeleteIcon } = ChakraIcons;

export function DeleteClientActionCell<D extends object>({
  value,
}: CellProps<D, string>) {
  const { mutate: deleteClient } = useRemoveClient();
  const { isOpen, onOpen, onClose } = useDisclosure();
  const throttledDeleteClient = useThrottle(() => {
    deleteClient(value);
    onClose();
  });
  const { t } = useTranslation();

  return (
    <>
      <Button variant="link" onClick={onOpen}>
        <DeleteIcon color="yolt.destructive" />
      </Button>
      <Modal
        isOpen={isOpen}
        onClose={onClose}
        title={t("management.deleteClient.header")}
        primaryAction={{
          label: t("button.delete"),
          onClick: throttledDeleteClient,
          variant: "destructive",
        }}
        secondaryAction={{ label: t("button.cancel"), onClick: onClose }}
      >
        {t("management.deleteClient.body")}
      </Modal>
    </>
  );
}
