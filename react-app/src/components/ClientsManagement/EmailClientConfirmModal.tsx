import { Button, Chakra, FormField, Input } from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { ClientEmailTemplatesRequest } from "types/management/client";

import {
  useCreateClientEmailTemplates,
  useEditClientEmailTemplates,
} from "api/management/clients";
import useThrottle from "helpers/useThrottle";

import { isAxiosError } from "utils/type-utils";

import type { EmailValues } from "pages/management/ManagementClientDetails/components/EmailTemplate/Template";
import type { ViolationsResponse } from "types/admin/violation";

const {
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalHeader,
  ModalOverlay,
  useToast,
  Box,
} = Chakra;

const EMAIL_TEMPLATE_TOAST_ID = "CLIENT_EMAI";

type CreateClientModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose"> & {
  id?: string;
} & EmailValues;

type Values = {
  reason: string;
  ticket: string;
};

export default function CreateClientModal({
  id,
  isOpen,
  onClose: closeModal,
}: CreateClientModalProps) {
  const { clientId } = useParams<{ clientId: string }>();
  const toast = useToast();
  const { t } = useTranslation();
  const { mutateAsync: createClientEmail } =
    useCreateClientEmailTemplates(clientId);
  const { mutateAsync: editClientEmail } =
    useEditClientEmailTemplates(clientId);
  const handleError = useErrorHandler();

  function onClose() {
    toast.close(EMAIL_TEMPLATE_TOAST_ID);
    closeModal();
  }

  const throttledDeleteClient = useThrottle(
    async (
      query: ClientEmailTemplatesRequest,
      formikHelpers: FormikHelpers<ClientEmailTemplatesRequest>
    ) => {
      try {
        id
          ? await editClientEmail({ ...query, id })
          : await createClientEmail(query);
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
          if (!toast.isActive(EMAIL_TEMPLATE_TOAST_ID)) {
            toast({
              title: id
                ? t("management.email.editFailed")
                : t("management.email.createFailed"),
              status: "error",
              isClosable: true,
              position: "top",
              duration: null,
              id: EMAIL_TEMPLATE_TOAST_ID,
            });
          }
        }
      }
    }
  );
  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>{t("management.createClient")}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Formik<Values>
              initialValues={{
                reason: "",
                ticket: "",
              }}
              onSubmit={throttledDeleteClient}
            >
              {(formikProps) => (
                <Form>
                  <Box>
                    <FormField
                      id="ticket"
                      label={t("management.ticket")}
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
                  <Box>
                    <FormField
                      id="reason"
                      label={t("management.reason")}
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
                  <Box display="flex" justifyContent="center" marginTop={2}>
                    <Button type="submit" variant="primary">
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
  );
}
