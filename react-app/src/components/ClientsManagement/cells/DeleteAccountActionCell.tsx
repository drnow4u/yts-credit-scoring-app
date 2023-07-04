import {
  Button,
  Chakra,
  ChakraIcons,
  FormField,
  Input,
} from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { AccountDeleteDTO } from "types/management/client";

import { useRemoveAccount } from "api/management/clients";

import { isAxiosError } from "utils/type-utils";

import type { CellProps } from "react-table";
import type { ViolationsResponse } from "types/admin/violation";

const { DeleteIcon } = ChakraIcons;

const {
  Modal,
  ModalBody,
  ModalCloseButton,
  useDisclosure,
  ModalContent,
  ModalHeader,
  ModalOverlay,
  useToast,
  Box,
  Text,
} = Chakra;

const DELETE_ACCOUNT_TOAST_ID = "DELETE_ACCOUNT";

export function DeleteAccountActionCell<D extends object>({
  value,
}: CellProps<D, string>) {
  const toast = useToast();
  const { mutate: deleteAccount } = useRemoveAccount();
  const { isOpen, onOpen, onClose: CloseModal } = useDisclosure();
  const { t } = useTranslation();
  const handleError = useErrorHandler();

  function onClose() {
    toast.close(DELETE_ACCOUNT_TOAST_ID);
    CloseModal();
  }

  const onSubmit = async (
    query: AccountDeleteDTO,
    formikHelpers: FormikHelpers<AccountDeleteDTO>
  ) => {
    try {
      await deleteAccount(query);
      onClose();
    } catch (e) {
      if (isAxiosError<ViolationsResponse>(e) && e.response?.status === 400) {
        const fields: { [field: string]: string } = {};
        e.response?.data.violations.forEach(
          (violation) => (fields[violation.fieldName] = violation.message)
        );
        formikHelpers.setErrors(fields);
      } else if (
        isAxiosError<ViolationsResponse>(e) &&
        e.response?.status === 401
      ) {
        handleError(e);
      } else {
        if (!toast.isActive(DELETE_ACCOUNT_TOAST_ID)) {
          toast({
            title: t("management.createFailed"),
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: DELETE_ACCOUNT_TOAST_ID,
          });
        }
      }
    }
  };

  return (
    <>
      <Button variant="link" onClick={onOpen}>
        <DeleteIcon color="yolt.destructive" />
      </Button>
      <Modal isOpen={isOpen} onClose={onClose}>
        <ModalOverlay>
          <ModalContent>
            <ModalHeader>{t("management.deleteAccount.header")}</ModalHeader>
            <ModalCloseButton />
            <ModalBody>
              <Box>
                <Text>{t("management.deleteAccount.body")}</Text>
              </Box>
              <Formik<AccountDeleteDTO>
                initialValues={{
                  id: value,
                  ticket: "",
                  reason: "",
                }}
                onSubmit={onSubmit}
              >
                {(formikProps) => (
                  <Form>
                    <Box>
                      <FormField
                        id="reason"
                        label={t("management.users.reason")}
                        isRequired
                        errorMessage={formikProps.errors.reason}
                      >
                        <Input
                          id="reason"
                          onChange={formikProps.handleChange}
                          onBlur={formikProps.handleBlur}
                          value={formikProps.values.reason}
                          name="reason"
                        />
                      </FormField>
                    </Box>
                    <Box>
                      <FormField
                        id="ticket"
                        label={t("management.users.ticket")}
                        isRequired
                        errorMessage={formikProps.errors.ticket}
                      >
                        <Input
                          id="ticket"
                          onChange={formikProps.handleChange}
                          onBlur={formikProps.handleBlur}
                          value={formikProps.values.ticket}
                          name="ticket"
                        />
                      </FormField>
                    </Box>
                    <Box display="flex" justifyContent="center" marginTop={2}>
                      <Button
                        type="submit"
                        variant="primary"
                        isDisabled={formikProps.isSubmitting}
                      >
                        {t("button.save")}
                      </Button>
                    </Box>
                  </Form>
                )}
              </Formik>
            </ModalBody>
          </ModalContent>
        </ModalOverlay>
      </Modal>
    </>
  );
}
