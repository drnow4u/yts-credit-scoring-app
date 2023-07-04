import React from "react";

import { Chakra, Toggle } from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { ViolationsResponse } from "types/admin/violation";
import { ClientUpdateFeatureToggle } from "types/management/client";

import { useUpdateFeatureToggle } from "api/management/clients";

import { isAxiosError } from "utils/type-utils";

const { Box, SimpleGrid, GridItem, Heading, Text, Flex, useToast } = Chakra;

type FeatureToggleProps = {
  overview?: boolean;
  months?: boolean;
  categories?: boolean;
  apiToken?: boolean;
  pDScoreFeature?: boolean;
  signatureVerification?: boolean;
};

const defaultFeatureToggle: FeatureToggleProps = {
  overview: true,
  months: false,
  categories: false,
  apiToken: false,
  pDScoreFeature: false,
  signatureVerification: false,
};

type ClientUpdateFeatureToggleValues = Omit<ClientUpdateFeatureToggle, "id">;

const UPDATE_CLIENT_FEATURE_TOGGLE_TOAST_ID =
  "UPDATE_CLIENT_FEATURE_TOGGLE_TOAST_ID";

export const FeatureToogle = (props: FeatureToggleProps) => {
  const { t } = useTranslation();
  const { clientId } = useParams<{ clientId: string }>();
  const { mutateAsync: updateFeatureToggle } = useUpdateFeatureToggle();
  const handleError = useErrorHandler();
  const toast = useToast();

  const onSubmit = async (
    values: ClientUpdateFeatureToggleValues,
    formikHelpers: FormikHelpers<ClientUpdateFeatureToggleValues>
  ) => {
    try {
      if (!clientId) return;
      await updateFeatureToggle({ id: clientId, ...values });
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
        if (!toast.isActive(UPDATE_CLIENT_FEATURE_TOGGLE_TOAST_ID)) {
          toast({
            title: "Failed to update toggle",
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: UPDATE_CLIENT_FEATURE_TOGGLE_TOAST_ID,
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
      maxH="330px"
      w={{ sm: "90%", xl: "95%" }}
      backdropFilter="saturate(200%) blur(50px)"
      backgroundColor="white"
      boxShadow="0px 2px 5.5px rgba(0, 0, 0, 0.02)"
      border="2px solid"
      borderColor="white"
      p="24px"
      borderRadius="20px"
    >
      <Box>
        <Box m="5">
          <Heading mt="0" as="h4" size="md">
            Feature toggle
          </Heading>
          <Text mt="0" mb={4}>
            Select feture toggle for client
          </Text>
          <Formik<ClientUpdateFeatureToggleValues>
            initialValues={{
              ...defaultFeatureToggle,
              ...props,
            }}
            onSubmit={onSubmit}
          >
            {(formikProps) => (
              <Form>
                <SimpleGrid columns={2} spacingX="40px" spacingY="20px">
                  <GridItem colSpan={1}>
                    <Toggle
                      id="overview"
                      value="overview"
                      checked={formikProps.values.overview}
                      defaultChecked={formikProps.values.overview}
                      {...(formikProps.errors.overview &&
                        formikProps.touched.overview && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue("overview", values);
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.overview`)}
                    </Toggle>
                  </GridItem>
                  <GridItem colSpan={1}>
                    <Toggle
                      id="months"
                      value="months"
                      checked={formikProps.values.months}
                      defaultChecked={formikProps.values.months}
                      {...(formikProps.errors.months &&
                        formikProps.touched.months && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue("months", values);
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.months`)}
                    </Toggle>
                  </GridItem>
                  <GridItem colSpan={1}>
                    <Toggle
                      id="categories"
                      value="categories"
                      checked={formikProps.values.categories}
                      defaultChecked={formikProps.values.categories}
                      {...(formikProps.errors.categories &&
                        formikProps.touched.categories && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue("categories", values);
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.categories`)}
                    </Toggle>
                  </GridItem>
                  <GridItem colSpan={1}>
                    <Toggle
                      id="apiToken"
                      value="apiToken"
                      checked={formikProps.values.apiToken}
                      defaultChecked={formikProps.values.apiToken}
                      {...(formikProps.errors.apiToken &&
                        formikProps.touched.apiToken && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue("apiToken", values);
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.apiToken`)}
                    </Toggle>
                  </GridItem>
                  <GridItem colSpan={1}>
                    <Toggle
                      id="pDScoreFeature"
                      value="pDScoreFeature"
                      checked={formikProps.values.pDScoreFeature}
                      defaultChecked={formikProps.values.pDScoreFeature}
                      {...(formikProps.errors.pDScoreFeature &&
                        formikProps.touched.pDScoreFeature && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue("pDScoreFeature", values);
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.pDScoreFeature`)}
                    </Toggle>
                  </GridItem>
                  <GridItem colSpan={1}>
                    <Toggle
                      id="signatureVerification"
                      value="signatureVerification"
                      checked={formikProps.values.signatureVerification}
                      defaultChecked={formikProps.values.signatureVerification}
                      {...(formikProps.errors.signatureVerification &&
                        formikProps.touched.signatureVerification && {
                          color: "red",
                        })}
                      onChange={(e: React.ChangeEvent<HTMLInputElement>) => {
                        const values = e.target.checked;
                        formikProps.setFieldValue(
                          "signatureVerification",
                          values
                        );
                        formikProps.handleSubmit();
                      }}
                    >
                      {t(`management.featureToggle.signatureVerification`)}
                    </Toggle>
                  </GridItem>
                </SimpleGrid>
              </Form>
            )}
          </Formik>
        </Box>
      </Box>
    </Flex>
  );
};

export default FeatureToogle;
