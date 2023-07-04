import CodeEditor from "@uiw/react-textarea-code-editor";
import { Button, Chakra, FormField, Input } from "@yolt/design-system";
import { Form, Formik } from "formik";
import { useTranslation } from "react-i18next";
import { ClientEmailTemplatesRequest } from "types/management/client";

import useThrottle from "helpers/useThrottle";

import EmailClientConfirmModal from "components/ClientsManagement/EmailClientConfirmModal";

const { Box, Image, Flex, Divider, useDisclosure } = Chakra;

export type Props = {
  id?: string;
  logo?: string;
  title?: string;
  subTitle?: string;
  welcomeBox?: string;
  buttonText?: string;
  summaryBox?: string;
};

export type EmailValues = Omit<
  ClientEmailTemplatesRequest,
  "id" | "reason" | "ticket"
>;

export default function Template(props: Props) {
  const { t } = useTranslation();
  const { isOpen, onOpen, onClose } = useDisclosure();

  const throttledDeleteClient = useThrottle((values: any) => {
    onOpen();
  });

  return (
    <Formik<EmailValues>
      initialValues={{
        title: "",
        subTitle: "",
        welcomeBox: "",
        summaryBox: "",
        buttonText: "",
        name: "",
        ...props,
      }}
      onSubmit={throttledDeleteClient}
    >
      {(formikProps) => (
        <Form>
          <Box marginBottom={10}>
            <FormField
              id="name"
              label={t("management.updateEmail.templateName")}
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
          <Box marginTop={2}>
            <Box marginLeft="10%" width="80%" border={1} borderColor="black">
              <Box backgroundColor="yolt.darkBlue" padding={3} />
              <Box position="relative">
                <Image
                  zIndex={-1}
                  position="absolute"
                  height={300}
                  width="100%"
                  src="https://content.mailplus.nl/m17/images/ts/yolt/2020___Yolt_White__Newsletter__e_mail/images/bg_hdr.png"
                />
              </Box>
              <Flex>
                <Box flex="50%">
                  <Image
                    width="50%"
                    marginTop="5%"
                    marginLeft="20%"
                    src="https://content.mailplus.nl/m17/images/ts/yolt/2020___Yolt_White__Newsletter__e_mail/images/logo.png"
                  />
                </Box>
                <Box flex="50%">
                  <Image marginTop="5%" marginLeft="10%" src={props.logo} />
                </Box>
              </Flex>
              <Flex display="flex" alignItems="center" justifyItems="center">
                <Box width="80%" margin="auto">
                  <FormField
                    id="title"
                    label={t("management.updateEmail.title")}
                    isRequired
                    errorMessage={formikProps.errors.title}
                  >
                    <Input
                      id="title"
                      onChange={formikProps.handleChange}
                      onBlur={formikProps.handleBlur}
                      value={formikProps.values.title}
                      name="title"
                    />
                  </FormField>
                </Box>
              </Flex>
              <Flex
                marginTop={5}
                display="flex"
                alignItems="center"
                justifyItems="center"
              >
                <Box width="80%" margin="auto">
                  <FormField
                    id="subTitle"
                    label={t("management.updateEmail.subTitle")}
                    isRequired
                    errorMessage={formikProps.errors.subTitle}
                  >
                    <Input
                      id="subTitle"
                      onChange={formikProps.handleChange}
                      onBlur={formikProps.handleBlur}
                      value={formikProps.values.subTitle}
                      name="subTitle"
                    />
                  </FormField>
                </Box>
              </Flex>
              <Flex
                marginTop={5}
                display="flex"
                alignItems="center"
                justifyItems="center"
              >
                <Box width="80%" margin="auto">
                  <CodeEditor
                    value={formikProps.values.welcomeBox}
                    language="html"
                    placeholder={t("management.updateEmail.htmlPlaceholder")}
                    onChange={(evn) => {
                      formikProps.setFieldValue("welcomeBox", evn.target.value);
                    }}
                    padding={15}
                    style={{
                      fontSize: 12,
                      backgroundColor: "#f5f5f5",
                      fontFamily:
                        "ui-monospace,SFMono-Regular,SF Mono,Consolas,Liberation Mono,Menlo,monospace",
                    }}
                  />
                </Box>
              </Flex>
              <Box marginTop={5}>
                <Divider marginBottom={10} />
                <Box width="50%" margin="auto">
                  <FormField
                    id="buttonText"
                    label={t("management.updateEmail.button")}
                    isRequired
                    errorMessage={formikProps.errors.buttonText}
                  >
                    <Input
                      placeholder={t(
                        "management.updateEmail.buttonPlaceholder"
                      )}
                      id="buttonText"
                      onChange={formikProps.handleChange}
                      onBlur={formikProps.handleBlur}
                      value={formikProps.values.buttonText}
                      name="buttonText"
                    />
                  </FormField>
                </Box>
                <Divider marginTop={10} />
              </Box>
              <Flex
                mt={5}
                display="flex"
                alignItems="center"
                justifyItems="center"
              >
                <Box width="80%" margin="auto">
                  <CodeEditor
                    value={formikProps.values.summaryBox}
                    language="html"
                    placeholder={t("management.updateEmail.htmlPlaceholder")}
                    onChange={(evn) => {
                      formikProps.setFieldValue("summaryBox", evn.target.value);
                    }}
                    padding={15}
                    style={{
                      fontSize: 12,
                      backgroundColor: "#f5f5f5",
                      fontFamily:
                        "ui-monospace,SFMono-Regular,SF Mono,Consolas,Liberation Mono,Menlo,monospace",
                    }}
                  />
                </Box>
              </Flex>
              <Flex
                mt={5}
                display="flex"
                alignItems="center"
                justifyItems="center"
              >
                <Box display="inline-block" margin="auto">
                  <Button type="submit">
                    {props.id ? t("button.update") : t("button.save")}
                  </Button>
                </Box>
              </Flex>
            </Box>
          </Box>
          <EmailClientConfirmModal
            isOpen={isOpen}
            onClose={onClose}
            id={props.id}
            {...formikProps.values}
          />
        </Form>
      )}
    </Formik>
  );
}
