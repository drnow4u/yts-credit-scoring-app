import {
  Button,
  Chakra,
  CopyButton,
  FormField,
  Input,
  Toggle,
} from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";

import { useCreateApiKey, useGetApiKeyPermissions } from "api/admin/api-keys";

import Loading from "components/Loading";

import { isAxiosError } from "utils/type-utils";

import type { ApiKeyDetails } from "types/admin/api-key";
import type { ViolationsResponse } from "types/admin/violation";

const {
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  SimpleGrid,
  useToast,
  Text,
  Alert,
  AlertIcon,
  Box,
  AlertDescription,
  HStack,
} = Chakra;

const INVITE_TOAST_ID = "CREATE_API_KEY";

type CreateApiKeyModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

function CreateApiKeyModal(props: CreateApiKeyModalProps) {
  const { t } = useTranslation();
  const {
    mutateAsync: createApiKey,
    data,
    isLoading,
    reset,
  } = useCreateApiKey();

  const { data: apiKeyPermissions, isLoading: isLoadingPermissions } =
    useGetApiKeyPermissions();

  const handleError = useErrorHandler();
  const toast = useToast();

  function onClose() {
    toast.close(INVITE_TOAST_ID);
    props.onClose();
    reset();
  }

  const apiKey = data?.data.clientToken;
  const apiKeysPermissions = apiKeyPermissions?.data;

  const onSubmit = async (
    values: ApiKeyDetails,
    formikHelpers: FormikHelpers<ApiKeyDetails>
  ) => {
    try {
      await createApiKey(values);
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
        if (!toast.isActive(INVITE_TOAST_ID)) {
          toast({
            title: "Failed to create api token",
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: INVITE_TOAST_ID,
          });
        }
      }
    }
  };

  return (
    <Modal {...props} onClose={onClose}>
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>{t("settings.newApiToken")}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Alert
              border="1px"
              borderColor="yolt.darkBlue"
              colorScheme="blue"
              marginBottom="4"
              status="warning"
            >
              <AlertIcon />
              <Box flex="1">
                <AlertDescription textColor="yolt.darkBlue" display="block">
                  {t("settings.description")}
                </AlertDescription>
              </Box>
            </Alert>
            {!apiKey ? (
              <Formik<ApiKeyDetails>
                initialValues={{
                  name: "",
                  permissions: [],
                }}
                validate={(values) => {
                  const errors = {} as {
                    permissions: Boolean;
                  };
                  if (values.permissions.length < 1) errors.permissions = true;

                  return errors;
                }}
                onSubmit={onSubmit}
              >
                {(formikProps) => (
                  <Form>
                    <FormField
                      id="name"
                      label={t("general.userName")}
                      isRequired
                    >
                      <Input
                        id="name"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.name}
                        name="name"
                      />
                    </FormField>
                    <SimpleGrid columns={2} spacingX="40px" spacingY="20px">
                      {isLoadingPermissions && <Loading />}
                      {apiKeysPermissions?.map((permission) => (
                        <Toggle
                          key={permission}
                          id={permission}
                          value={permission}
                          {...(formikProps.errors.permissions &&
                            formikProps.touched.permissions && {
                              color: "red",
                            })}
                          onChange={(e) => {
                            const values = e.target.checked
                              ? [...formikProps.values.permissions, permission]
                              : formikProps.values.permissions.filter(
                                  (entity) => entity !== e.target.value
                                );

                            formikProps.setFieldValue("permissions", values);
                          }}
                        >
                          {t(`settings.permissions.${permission}` as any)}
                        </Toggle>
                      ))}
                    </SimpleGrid>
                    <Button
                      type="submit"
                      variant="primary"
                      isDisabled={formikProps.isSubmitting}
                      isLoading={isLoading}
                    >
                      {t("button.save")}
                    </Button>
                  </Form>
                )}
              </Formik>
            ) : (
              <HStack
                direction="column"
                wrap={"wrap"}
                alignItems="center"
                justifyContent="center"
              >
                <Text display="block" textAlign="center">
                  {apiKey}
                </Text>
                <CopyButton aria-label="copy" value={apiKey} />
              </HStack>
            )}
          </ModalBody>
          <ModalFooter />
        </ModalContent>
      </ModalOverlay>
    </Modal>
  );
}

export default CreateApiKeyModal;
