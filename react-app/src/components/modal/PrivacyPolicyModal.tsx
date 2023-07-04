import { Chakra } from "@yolt/design-system";

import { usePrivacyPolicy } from "api/privacy-policy";

const {
  Box,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
} = Chakra;

type PrivacyPolicyModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

function PrivacyPolicyModal(props: PrivacyPolicyModalProps) {
  const { data: privacyPolicy } = usePrivacyPolicy();

  return (
    <Modal {...props} size="full">
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>Privacy Policy</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Box width="calc(100vw - 48px)" height="calc(100vh - 128px)">
              <iframe
                width="100%"
                height="100%"
                style={{ overflow: "scroll" }}
                title="Privacy Policy"
                srcDoc={privacyPolicy?.html}
              />
            </Box>
          </ModalBody>
          <ModalFooter />
        </ModalContent>
      </ModalOverlay>
    </Modal>
  );
}

export default PrivacyPolicyModal;
