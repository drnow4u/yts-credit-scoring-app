import { Button, Chakra, FormField, Input, Select } from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { AccountCreateDTO } from "types/management/client";

import { useCreateAccount } from "api/management/clients";

import { isAxiosError } from "utils/type-utils";

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

const CREATE_ACCOUNT_TOAST_ID = "CREATE_USER";

const providers = [
  {
    id: "github",
    name: "Github",
  },
  {
    id: "google",
    name: "Google",
  },
  {
    id: "Microsoft",
    name: "microsoft",
  },
];

type CreateAccountModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

export default function CreateAccountModal(props: CreateAccountModalProps) {
  const toast = useToast();
  const { t } = useTranslation();
  const { mutateAsync: createAccount } = useCreateAccount();
  const handleError = useErrorHandler();

  function onClose() {
    toast.close(CREATE_ACCOUNT_TOAST_ID);
    props.onClose();
  }

  const onSubmit = async (
    query: AccountCreateDTO,
    formikHelpers: FormikHelpers<AccountCreateDTO>
  ) => {
    try {
      await createAccount(query);
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
        if (!toast.isActive(CREATE_ACCOUNT_TOAST_ID)) {
          toast({
            title: t("management.createFailed"),
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: CREATE_ACCOUNT_TOAST_ID,
          });
        }
      }
    }
  };

  return (
    <Modal {...props} onClose={onClose}>
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>{t("management.createAccount")}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Formik<AccountCreateDTO>
              initialValues={{
                userName: "",
                id: "",
                email: "",
                provider: "",
                ticket: "",
                reason: "",
              }}
              onSubmit={onSubmit}
            >
              {(formikProps) => (
                <Form>
                  <Box>
                    <FormField
                      id="userName"
                      label={t("management.users.userName")}
                      isRequired
                      errorMessage={formikProps.errors.userName}
                    >
                      <Input
                        id="userName"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.userName}
                        name="userName"
                      />
                    </FormField>
                  </Box>
                  <Box>
                    <FormField
                      id="id"
                      label="ID"
                      isRequired
                      errorMessage={formikProps.errors.id}
                    >
                      <Input
                        id="id"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.id}
                        name="id"
                      />
                    </FormField>
                  </Box>
                  <Box>
                    <FormField
                      id="email"
                      label={t("management.users.email")}
                      isRequired
                      errorMessage={formikProps.errors.email}
                    >
                      <Input
                        id="email"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.email}
                        name="email"
                      />
                    </FormField>
                  </Box>
                  <Box>
                    <FormField
                      id="provider"
                      label={t("management.users.provider")}
                      errorMessage={formikProps.errors.provider}
                      isRequired
                    >
                      <Select
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.provider}
                        name="provider"
                        isRequired
                      >
                        {providers.map((option) => (
                          <option key={option.id} value={option.id}>
                            {option.name}
                          </option>
                        ))}
                      </Select>
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
  );
}
