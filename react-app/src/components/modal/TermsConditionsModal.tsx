import { Chakra } from "@yolt/design-system";

import { useTermsConditions } from "api/terms-conditions";

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

type TermsConditionsModalProps = Pick<Chakra.ModalProps, "isOpen" | "onClose">;

function TermsConditionsModal(props: TermsConditionsModalProps) {
  const { data: termsConditions } = useTermsConditions();

  return (
    <Modal {...props} size="full">
      <ModalOverlay>
        <ModalContent>
          <ModalHeader>Terms and Conditions</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Box width="calc(100vw - 48px)" height="calc(100vh - 128px)">
              <iframe
                width="100%"
                height="100%"
                style={{ overflow: "scroll" }}
                title="Terms and Conditions"
                srcDoc={termsConditions?.html}
              />
            </Box>
          </ModalBody>
          <ModalFooter />
        </ModalContent>
      </ModalOverlay>
    </Modal>
  );
}

export default TermsConditionsModal;
