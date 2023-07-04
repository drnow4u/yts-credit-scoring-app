import { Chakra } from "@yolt/design-system";

const { Heading, Stack } = Chakra;

function NotFound() {
  return (
    <Stack as="main">
      <Heading as="h1">Page Not Found</Heading>
    </Stack>
  );
}

export default NotFound;
