import { useState } from "react";

import { Button, Chakra, ChakraIcons, CopyButton } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { Stack, Text } = Chakra;
const { LockIcon, UnlockIcon } = ChakraIcons;

export interface SignatureInfoProps {
  signatureVerified: boolean;
  signature: string;
}

function SignatureInfo({ signature, signatureVerified }: SignatureInfoProps) {
  const { t } = useTranslation();
  const [signatureVisible, setSignatureVisible] = useState(false);
  return (
    <Stack alignItems="center" justifyContent="center">
      <Stack
        alignItems="center"
        justifyContent="center"
        spacing="4"
        bg="white"
        padding="1rem"
      >
        <Stack isInline alignItems="center" spacing="2">
          {signatureVerified ? (
            <>
              <LockIcon opacity="0.3" />
              <Text isTruncated opacity="0.3">
                {t("report.signatureInfo.verified")}
              </Text>
            </>
          ) : (
            <>
              <UnlockIcon color="red.500" />
              <Text isTruncated color="red.500">
                {t("report.signatureInfo.failed")}
              </Text>
            </>
          )}
        </Stack>
        <Stack
          isInline
          alignItems="center"
          spacing="2"
          opacity="0.3"
          inlineSize="sm"
          justifyContent="center"
        >
          {signatureVisible ? (
            <>
              <Text isTruncated>
                {`${signature.substring(0, 8)}...${signature.substring(
                  signature.length - 8,
                  signature.length
                )}`}
              </Text>
              <CopyButton aria-label="copy" value={signature} />
            </>
          ) : (
            <Button onClick={() => setSignatureVisible(true)}>
              {t("report.signatureInfo.showSignature")}
            </Button>
          )}
        </Stack>
      </Stack>
    </Stack>
  );
}

export default SignatureInfo;
