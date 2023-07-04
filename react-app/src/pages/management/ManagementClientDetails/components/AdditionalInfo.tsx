import {
  Button,
  Chakra,
  FormField,
  Input,
  Textarea,
} from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { ViolationsResponse } from "types/admin/violation";
import { ClientUpdateDTO } from "types/management/client";

import { useUpdateClient } from "api/management/clients";

import { isAxiosError } from "utils/type-utils";

const { Box, Heading, Text, Flex, useToast } = Chakra;

type AdditionalInfoProps = {
  redirectUrl?: string;
  additionalReportText?: string;
  additionalConsentText?: string;
};

type ClientUpdateValues = Omit<ClientUpdateDTO, "id">;

const UPDATE_CLIENT_DETAILS_TOAST_ID = "UPDATE_CLIENT_DETAILS_TOAST_ID";

export const AdditionalInfo = (props: AdditionalInfoProps) => {
  const { t } = useTranslation();
  const { clientId } = useParams<{ clientId: string }>();
  const { mutateAsync: updateClient } = useUpdateClient();
  const toast = useToast();

  const handleError = useErrorHandler();

  const onSubmit = async (
    values: ClientUpdateValues,
    formikHelpers: FormikHelpers<ClientUpdateValues>
  ) => {
    try {
      if (!clientId) return;

      await updateClient({ id: clientId, ...values });
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
        if (!toast.isActive(UPDATE_CLIENT_DETAILS_TOAST_ID)) {
          toast({
            title: "Failed to update additional info",
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: UPDATE_CLIENT_DETAILS_TOAST_ID,
          });
        }
      }
    }
  };

  return (
    <Flex
      marginTop={4}
      direction={{ sm: "column", md: "row" }}
      mx="1.5rem"
      w={{ sm: "90%", xl: "60%" }}
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
            Additional information
          </Heading>
          <Text mt="0" mb={4}>
            Update below information
          </Text>
          <Formik<ClientUpdateValues>
            initialValues={{
              redirectUrl: "",
              additionalReportText: "",
              additionalConsentText: "",
              ...props,
            }}
            onSubmit={onSubmit}
          >
            {(formikProps) => (
              <Form>
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
                <Box marginTop={4} display="flex" justifyContent="center">
                  <Button type="submit">{t("button.save")}</Button>
                </Box>
              </Form>
            )}
          </Formik>
        </Box>
      </Box>
    </Flex>
  );
};

export default AdditionalInfo;
