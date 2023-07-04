import { Chakra } from "@yolt/design-system";

import type { ReactNode } from "react";

const { Center, Stack } = Chakra;

interface ErrorProps {
  children: ReactNode;
}

function Error({ children = null }: ErrorProps) {
  return (
    <Center as="main" display="flex" height="100%">
      <Stack margin="0 auto" padding="2rem" bg="white" align="center">
        {children}
      </Stack>
    </Center>
  );
}

export default Error;
