import { Button, Chakra, FormField, Select } from "@yolt/design-system";
import { Form, Formik } from "formik";
import { useTranslation } from "react-i18next";

import { useConnectSite, useSites } from "api/site";

const { Center, Heading, Stack, Box, useToast } = Chakra;

const CONNECT_SITE_TOAST_ID = "connect_site";

function SelectSite() {
  const toast = useToast();
  const { t } = useTranslation();
  const { data: sites } = useSites();
  const { mutateAsync: connectSite } = useConnectSite();

  if (!sites) {
    return null;
  }

  const onSubmit = async (values: { site: string }) => {
    try {
      const response = await connectSite(values.site);
      if (!response) {
        throw new Error("Connecting to site failed");
      }
      window.location.assign(response.data.redirectUrl);
    } catch (error: any) {
      toast({
        title: error.message,
        status: "error",
        isClosable: true,
        position: "top",
        id: CONNECT_SITE_TOAST_ID,
      });
    }
  };

  return (
    <Center as="main" display="flex" height="100%">
      <Stack
        display="flex"
        spacing="2rem"
        alignItems="center"
        justifyContent="center"
        padding="16px"
        margin="16px"
        bg="white"
      >
        <Heading as="h1">{t("site.select")}</Heading>
        <Formik<{ site: string }>
          initialValues={{
            site: "",
          }}
          onSubmit={onSubmit}
        >
          {(formikProps) => (
            <Form>
              <FormField id="site">
                <Select
                  onChange={formikProps.handleChange}
                  onBlur={formikProps.handleBlur}
                  value={formikProps.values.site}
                  name="site"
                >
                  {sites
                    .sort((siteA, siteB) =>
                      siteA.name.toLocaleLowerCase() >
                      siteB.name.toLocaleLowerCase()
                        ? 1
                        : -1
                    )
                    .map((site) => (
                      <option key={site.id} value={site.id}>
                        {site.name}
                      </option>
                    ))}
                </Select>
              </FormField>
              <Box marginTop={8}>
                <Button
                  type="submit"
                  isFullWidth
                  isDisabled={
                    formikProps.isSubmitting || !formikProps.values.site
                  }
                >
                  {t("button.next")}
                </Button>
              </Box>
            </Form>
          )}
        </Formik>
      </Stack>
    </Center>
  );
}

export default SelectSite;
