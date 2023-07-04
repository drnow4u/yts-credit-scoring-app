import {
  Button,
  Chakra,
  FormField,
  Input,
  Select,
  Textarea,
} from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { ClientCreateDTO } from "types/management/client";

import { useCreateClient } from "api/management/clients";

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
  SimpleGrid,
  GridItem,
} = Chakra;

const CREATE_CLIENT_TOAST_ID = "CREATE_CLIENT";

const siteTags = [
  {
    id: "site-tag-1",
    name: "site tag",
  },
  {
    id: "site-tag-2",
    name: "site tag 2",
  },
  {
    id: "site-tag-3",
    name: "site tag 3",
  },
  {
    id: "site-tag-4",
    name: "site tag 4",
  },
];

const languages = [
  {
    id: "en",
    name: "English",
  },
  {
    id: "fr",
    name: "French",
  },
  {
    id: "nl",
    name: "Nederlands",
  },
];

type CreateClientModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

export default function CreateClientModal(props: CreateClientModalProps) {
  const navigate = useNavigate();
  const toast = useToast();
  const { t } = useTranslation();
  const { mutateAsync: createClient } = useCreateClient();
  const handleError = useErrorHandler();

  function onClose() {
    toast.close(CREATE_CLIENT_TOAST_ID);
    props.onClose();
  }

  const onSubmit = async (
    query: ClientCreateDTO,
    formikHelpers: FormikHelpers<ClientCreateDTO>
  ) => {
    try {
      const { data } = await createClient(query);
      navigate(`/management/client/${data.id}`);
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
        if (!toast.isActive(CREATE_CLIENT_TOAST_ID)) {
          toast({
            title: t("management.createFailed"),
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: CREATE_CLIENT_TOAST_ID,
          });
        }
      }
    }
  };

  return (
    <Modal {...props} onClose={onClose}>
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>{t("management.createClient")}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Formik<ClientCreateDTO>
              initialValues={{
                name: "",
                siteTag: "",
                defaultLanguage: "en",
              }}
              onSubmit={onSubmit}
            >
              {(formikProps) => (
                <Form>
                  <Box>
                    <FormField
                      id="name"
                      label={t("management.invitation.clientName")}
                      isRequired
                      errorMessage={formikProps.errors.name}
                    >
                      <Input
                        id="name"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.name}
                        name="name"
                      />
                    </FormField>
                  </Box>
                  <SimpleGrid
                    marginTop={2}
                    columns={2}
                    columnGap={3}
                    rowGap={6}
                    w="full"
                  >
                    <GridItem colSpan={1}>
                      <FormField
                        id="siteTag"
                        label={t("management.invitation.siteTag")}
                        errorMessage={formikProps.errors.siteTag}
                        isRequired
                      >
                        <Select
                          onChange={formikProps.handleChange}
                          onBlur={formikProps.handleBlur}
                          value={formikProps.values.siteTag}
                          name="siteTag"
                          isRequired
                        >
                          {siteTags.map((option) => (
                            <option key={option.id} value={option.id}>
                              {option.name}
                            </option>
                          ))}
                        </Select>
                      </FormField>
                    </GridItem>
                    <GridItem colSpan={1}>
                      <FormField
                        id="defaultLanguage"
                        label={t("management.invitation.defaultLanguage")}
                        errorMessage={formikProps.errors.defaultLanguage}
                        isRequired
                      >
                        <Select
                          onChange={formikProps.handleChange}
                          onBlur={formikProps.handleBlur}
                          value={formikProps.values.defaultLanguage}
                          name="defaultLanguage"
                          isRequired
                        >
                          {languages.map((option) => (
                            <option key={option.id} value={option.id}>
                              {option.name}
                            </option>
                          ))}
                        </Select>
                      </FormField>
                    </GridItem>
                  </SimpleGrid>
                  <Box marginTop={2}>
                    <FormField
                      id="redirectUrl"
                      label={t("management.invitation.redirectUrl")}
                      errorMessage={formikProps.errors.redirectUrl}
                    >
                      <Input
                        id="redirectUrl"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.redirectUrl}
                        name="redirectUrl"
                      />
                    </FormField>
                  </Box>
                  <Box marginTop={2}>
                    <FormField
                      id="additionalReportText"
                      label={t("management.invitation.additionalReportText")}
                      errorMessage={formikProps.errors.additionalReportText}
                    >
                      <Textarea
                        id="additionalReportText"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.additionalReportText}
                        name="additionalReportText"
                      />
                    </FormField>
                  </Box>
                  <Box marginTop={2}>
                    <FormField
                      id="additionalConsentText"
                      label={t("management.invitation.additionalConsentText")}
                      errorMessage={formikProps.errors.additionalConsentText}
                    >
                      <Textarea
                        id="additionalConsentText"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.additionalConsentText}
                        name="additionalConsentText"
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
