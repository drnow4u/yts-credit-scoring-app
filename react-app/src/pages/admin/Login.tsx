import {
  Chakra,
  GithubSocialButton,
  GoogleSocialButton,
  MicrosoftSocialButton,
} from "@yolt/design-system";
import { Navigate, useLocation } from "react-router-dom";

import { useAccount } from "api/admin/account";
import { HOMEPAGE } from "helpers/constants";

const { Image, Stack, Flex, Box, Heading, Link } = Chakra;

function Login() {
  const location = useLocation();
  const account = useAccount();

  const { from } = (location.state as any) || {
    from: { pathname: "/admin/dashboard", search: location.search },
  };

  if (!!account.data) {
    return <Navigate to={from} replace />;
  }
  return (
    <Flex minH="100vh" alignItems="center" justify="center" position="relative">
      <Flex
        maxW="50%"
        alignSelf="flex-start"
        top={10}
        left={10}
        position="absolute"
        as="a"
        href="https://www.yolt.com"
      >
        <Image
          src={`${HOMEPAGE}YOLT_LOGO-HERO_GR-RGB.svg`}
          alt="Logo of Yolt"
          width="6vw"
          maxWidth="350px"
          margin="0 auto 3rem"
        />
      </Flex>
      <Stack spacing={8} mx={"auto"} maxW={"lg"} py={12} px={6}>
        <Stack align={"center"}>
          <Heading fontSize={"4xl"}>Sign in Cashflow Analyser</Heading>
        </Stack>
        <Box minW="450" rounded={"lg"} boxShadow={"lg"} bgColor="white" p={8}>
          <Stack alignItems="center" justify="center" spacing={4}>
            <Heading textAlign="center" fontSize={"4xl"}>
              Sign in
            </Heading>
            {/* <Text textAlign="center" textColor="yolt.lightGrey">
              In our free sandbox enviroment, you can connect to a test bank and
              set up a simple AIS and PIS calls
            </Text> */}
            <Stack width="75%">
              <Link
                _hover={{}}
                _focus={{}}
                _active={{}}
                textDecorationLine="none"
                href={`${HOMEPAGE}api/admin/oauth2/google`}
              >
                <GoogleSocialButton />
              </Link>
              <Link
                _hover={{}}
                _focus={{}}
                _active={{}}
                textDecorationLine="none"
                href={`${HOMEPAGE}api/admin/oauth2/microsoft`}
              >
                <MicrosoftSocialButton />
              </Link>
              <Link
                _hover={{}}
                _focus={{}}
                _active={{}}
                textDecorationLine="none"
                href={`${HOMEPAGE}api/admin/oauth2/github`}
              >
                <GithubSocialButton />
              </Link>
            </Stack>
          </Stack>
        </Box>
      </Stack>
    </Flex>
  );
}

export default Login;
