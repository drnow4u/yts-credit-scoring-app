import { Chakra, components } from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { ViolationsResponse } from "types/admin/violation";

import { useUploadClientLogo } from "api/management/clients";

import { isAxiosError } from "utils/type-utils";

const { Box, Heading, Flex, useToast } = Chakra;
const { FileInput } = components;

type LogoProps = {
  clientId: string;
  image?: any; // TODO When API will be ready
};

type values = {
  image: any;
};

const FILED_UPLOAD_CLIENT_LOGO_TOAST_ID = "FILED_UPLOAD_CLIENT_LOGO_TOAST_ID";

const FILED_UPLOAD_LOCAL_CLIENT_LOGO_TOAST_ID =
  "FILED_UPLOAD_LOCAL_CLIENT_LOGO_TOAST_ID";

const FILED_UPLOADEDED_SUCCESS = "FILED_UPLOADED_SUCCESS";

export const LogoUpload = ({ clientId, image }: LogoProps) => {
  const { t } = useTranslation();
  const { mutateAsync: uploadLogo } = useUploadClientLogo(clientId);
  const handleError = useErrorHandler();
  const toast = useToast();

  const onSubmit = async (
    values: values,
    formikHelpers: FormikHelpers<values>
  ) => {
    try {
      if (!clientId) return;
      const formData = new FormData();
      formData.append("logo", values.image);
      await uploadLogo(formData);
      if (!toast.isActive(FILED_UPLOADEDED_SUCCESS)) {
        toast({
          title: t(`management.logo.successUpload`),
          status: "success",
          isClosable: true,
          position: "top",
          duration: null,
          id: FILED_UPLOADEDED_SUCCESS,
        });
      }
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
        if (!toast.isActive(FILED_UPLOAD_CLIENT_LOGO_TOAST_ID)) {
          toast({
            title: t(`management.logo.filedUpdate`),
            status: "error",
            isClosable: true,
            position: "top",
            duration: null,
            id: FILED_UPLOAD_CLIENT_LOGO_TOAST_ID,
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
      p={4}
      borderRadius={4}
    >
      <Box>
        <Box m="5">
          <Heading mt="0" mb="2" as="h4" size="md">
            {t(`management.logo.cardTitle`)}
          </Heading>
          <Formik<values>
            initialValues={{
              image: image ? image : "",
            }}
            onSubmit={onSubmit}
          >
            {(formikProps) => (
              <Form>
                <FileInput
                  setValue={(value) => {
                    formikProps.setFieldValue("image", value);
                    formikProps.handleSubmit();
                  }}
                  onError={() => {
                    if (
                      !toast.isActive(FILED_UPLOAD_LOCAL_CLIENT_LOGO_TOAST_ID)
                    ) {
                      toast({
                        title: t(`management.logo.filedLogoProcess`),
                        status: "error",
                        isClosable: true,
                        position: "top",
                        duration: null,
                        id: FILED_UPLOAD_LOCAL_CLIENT_LOGO_TOAST_ID,
                      });
                    }
                  }}
                />
              </Form>
            )}
          </Formik>
        </Box>
      </Box>
    </Flex>
  );
};

export default LogoUpload;
