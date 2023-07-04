import { Button, Chakra, FormField, Input, Select } from "@yolt/design-system";
import { Form, Formik, FormikHelpers } from "formik";
import { useErrorHandler } from "react-error-boundary";
import { useTranslation } from "react-i18next";
import { InvitationTemplateOption } from "types/admin/invitation-template-option";

import { useInvitationTemplates } from "api/admin/invitation-template";
import { useInviteUser } from "api/admin/user";

import { isAxiosError } from "utils/type-utils";

import type { UserInvite } from "types/admin/user";
import type { ViolationsResponse } from "types/admin/violation";

const {
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  useToast,
  Box,
} = Chakra;

const INVITE_TOAST_ID = "USER_INVITE";

type InviteUserModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

function InviteUserModal(props: InviteUserModalProps) {
  const { mutateAsync: inviteUser } = useInviteUser();
  const { data: invitationTemplates } = useInvitationTemplates();
  const handleError = useErrorHandler();
  const toast = useToast();
  const { t } = useTranslation();

  const emptySelect: InvitationTemplateOption = {
    id: "",
    template: "",
  };
  const templateOptions = invitationTemplates ?? [];
  const preselectedOption =
    templateOptions.length === 1 ? templateOptions[0] : emptySelect;

  function onClose() {
    toast.close(INVITE_TOAST_ID);
    props.onClose();
  }

  const onSubmit = async (
    { clientEmailId, ...values }: UserInvite,
    formikHelpers: FormikHelpers<UserInvite>
  ) => {
    try {
      const query =
        templateOptions.length > 1
          ? {
              ...values,
              clientEmailId,
            }
          : values;

      await inviteUser(query);
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
        if (!toast.isActive(INVITE_TOAST_ID)) {
          toast({
            title: t("invitation.invitationFailed"),
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
          <ModalHeader>{t("invitation.inviteUser")}</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Formik<UserInvite>
              initialValues={{
                name: "",
                email: "",
                clientEmailId: preselectedOption.id,
              }}
              onSubmit={onSubmit}
            >
              {(formikProps) => (
                <Form>
                  <Box>
                    <FormField
                      id="name"
                      label={t("general.userName")}
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
                  <Box marginTop="1rem" marginBottom="1rem">
                    <FormField
                      id="email"
                      label={t("general.email")}
                      errorMessage={formikProps.errors.email}
                      isRequired
                    >
                      <Input
                        id="email"
                        onChange={formikProps.handleChange}
                        onBlur={formikProps.handleBlur}
                        value={formikProps.values.email}
                        name="email"
                        type="email"
                      />
                    </FormField>
                  </Box>
                  {templateOptions.length > 1 && (
                    <Box marginBottom="1rem">
                      <FormField
                        id="clientEmailId"
                        label={t("invitation.invitationMessageTemplateName")}
                        errorMessage={formikProps.errors.clientEmailId}
                        isRequired
                      >
                        <Select
                          onChange={formikProps.handleChange}
                          onBlur={formikProps.handleBlur}
                          value={formikProps.values.clientEmailId}
                          name="clientEmailId"
                          isRequired
                        >
                          {templateOptions.map((option) => (
                            <option key={option.id} value={option.id}>
                              {option.template}
                            </option>
                          ))}
                        </Select>
                      </FormField>
                    </Box>
                  )}
                  <Button
                    type="submit"
                    variant="primary"
                    isDisabled={formikProps.isSubmitting}
                  >
                    {t("invitation.invite")}
                  </Button>
                </Form>
              )}
            </Formik>
          </ModalBody>
          <ModalFooter />
        </ModalContent>
      </ModalOverlay>
    </Modal>
  );
}

export default InviteUserModal;
