import { Button, Chakra } from "@yolt/design-system";

import { HOMEPAGE } from "helpers/constants";

const { Image, Stack, Flex, Box, Heading, Text, Container, Link, HStack } =
  Chakra;

function Landing() {
  return (
    <>
      <Flex
        minH="100vh"
        alignItems="center"
        justify="center"
        position="relative"
      >
        <Flex
          as="a"
          maxW="50%"
          alignSelf="flex-start"
          top={10}
          left={10}
          position="absolute"
          href="https://www.yolt.com"
        >
          <Image
            src={`${HOMEPAGE}YOLT_LOGO-HERO_GR-RGB.svg`}
            alt="Logo of Yolt"
            width="6vw"
            maxWidth="350px"
            margin="0 auto 3rem"
            cursor="pointer"
          />
        </Flex>
        <Stack
          justifyContent="center"
          alignItems="center"
          spacing={8}
          mx={"auto"}
          maxW={"lg"}
          py={12}
          px={6}
        >
          <Stack align={"center"}>
            <Heading textColor="yolt.heroBlue" fontSize={"x-large"}>
              Cashflow Analyser
            </Heading>
            <Text textAlign="center" textColor="yolt.stormGrey">
              Cashflow Analyser is an online tool that helps lenders and leasing
              companies leverage open banking to provide immediate insight into
              the cash flow of your applications.
            </Text>
          </Stack>
          <HStack
            align={"center"}
            justifyContent="center"
            alignItems="center"
            width="60%"
            p={8}
          >
            <Stack alignItems="center" justify="center" spacing={4}>
              <Link width="100%" textDecor="none" href="admin/login">
                <Button isFullWidth>Sign in</Button>
              </Link>
              <Link
                width="100%"
                textDecor="none"
                href="https://www.yolt.com/about-us/contact-us"
              >
                <Button variant="secondary" isFullWidth>
                  Contact us
                </Button>
              </Link>
            </Stack>
          </HStack>
        </Stack>
      </Flex>
      <Box py={4} bg={"yolt.darkGrey"}>
        <Container
          as={Stack}
          maxW={"6xl"}
          py={4}
          direction={{ base: "column", md: "row" }}
          justify={{ base: "center", md: "flex-start" }}
          align={{ base: "center", md: "center" }}
        >
          <Stack
            as="a"
            href="https://www.yolt.com"
            position="relative"
            width={"20%"}
          >
            <Image
              src={`${HOMEPAGE}YOLT_LOGO-WHITE_GR-RGB.svg`}
              alt="Logo of Yolt"
              width="5vw"
              maxWidth="350px"
              position="absolute"
              left={25}
              top="-10px"
            />
          </Stack>
          <Stack gap={10} direction={{ base: "column", md: "row" }}>
            <Box as="a" color="white" href="admin/legal" fontWeight="bold">
              Legal
            </Box>
            <Box
              as="a"
              color="white"
              href="admin/privacy-statement"
              fontWeight="bold"
            >
              Privacy Policy
            </Box>
          </Stack>
        </Container>
        <Container
          as={Stack}
          maxW={"6xl"}
          py={4}
          direction={{ base: "column", md: "row" }}
          justify={{ base: "center", md: "flex-start" }}
          align={{ base: "center", md: "center" }}
        >
          <Stack width={"20%"} />
          <Stack width="60%" gap={10} direction={"row"}>
            <Text fontSize={12} textColor="yolt.stormGrey">
              Yolt and YTS are registered trademarks of Yolt Group BV. The YTS
              services in the UK are provided by Yolt Technology Services
              Limited (“YTS Limited”). YTS Limited is authorised by the
              Financial Conduct Authority under the Payment Service Regulations
              2017. You can find details on YTS Limited at register.fca.org.uk
              (reference number: 921127).
              <br />
              <br />
              YTS Limited's registered office is a 8-10 Moorgate, London EC2R
              6DA, United Kingdom, company number 12388678.
              <br />
              <br />
              The YTS services in the EU are provided by Yolt Technology
              Services B.V. (“YTS BV”) except for the regulated Account
              Information & Payment Initiation services (“TPP Functions”). The
              TPP functions are provided in the European Union by ING Bank N.V.
              (“ING”). ING has its statutory seat in Amsterdam at Bijlmerplein
              888, 1102 MG, The Netherlands, Trade Register number 33031431. ING
              is regulated and licensed by the De Nederlandsche Bank ('DNB') and
              the European Central Bank ('ECB'). ING is also regulated and
              licensed by the Autoriteit Financiële Markten ('AFM') and
              regulated by the Autoriteit Consument & Markt ('ACM'). Information
              regarding supervision of ING can be obtained from DNB
              (www.dnb.nl), AFM (www.afm.nl) or ACM (www.acm.nl). YTS B.V's
              registered office is at Hoogoorddreef 60, 1101 BE Amsterdam, The
              Netherlands, company number 76904814.
            </Text>
          </Stack>
        </Container>
      </Box>
    </>
  );
}

export default Landing;
