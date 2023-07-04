import { Chakra } from "@yolt/design-system";

import { HOMEPAGE } from "helpers/constants";

import { Paragraph, Text, Title } from "./components";

const { Heading, Stack, Image, Box, Flex, Container, Link, Divider } = Chakra;

export const PrivacyStatement = () => {
  return (
    <>
      <Box px={4}>
        <Flex h={16} alignItems={"center"} justifyContent={"space-between"}>
          <Link href="/admin">
            <Image
              src={`${HOMEPAGE}YOLT_LOGO-HERO_GR-RGB.svg`}
              alt="Logo of Yolt"
              width="6vw"
              maxWidth="350px"
              cursor="pointer"
            />
          </Link>
        </Flex>
      </Box>
      <Container maxW={"5xl"}>
        <Stack spacing={{ base: 8, md: 10 }} py={{ base: 20, md: 28 }}>
          <Heading
            textColor="yolt.heroBlue"
            fontWeight={600}
            fontSize={{ base: "3xl", sm: "4xl", md: "6xl" }}
            lineHeight={"110%"}
          >
            Privacy Statement
          </Heading>
          <Divider backgroundColor="yolt.heroBlue" />
          <Title>YOLT TECHNOLOGY SERVICES WEBSITE PRIVACY STATEMENT</Title>
          <Paragraph margin="md">
            <Text>
              This Website Privacy Statement governs the use of our website and
              communication channels like email and forms. By using our website
              and engaging with us through our communication channels you accept
              being bound by this Yolt Technology Services Website Privacy
              Statement.
            </Text>
            <Text>
              If you are a Yolt Technology Services client, your personal data
              and other data will be governed by the provisions as contractually
              agreed between your company and Yolt Technology Services.
            </Text>
          </Paragraph>
          <Title>WHAT PERSONAL DATA DO WE USE?</Title>
          <Paragraph margin="md">
            <Text>
              Personal data is any data relating to a person who is identified
              or who can be identified (such as a name, an identification
              number, or an online identifier).
            </Text>
            <Text>
              Personal data you give us: You may give us personal data about you
              by filling in forms on, or interacting with, our website, or by
              corresponding with us by phone, email or otherwise. Examples of
              personal data include:
            </Text>
            <Paragraph margin="xs">
              <Text dot>name, address, email address and phone number</Text>
              <Text dot>
                other information you submit to us e.g. by filling in forms, or
                as part of further inquiries into the Yolt Technology Services
              </Text>
            </Paragraph>
            <Text fontWeight={600} color={"gray.700"}>
              Personal data we collect from you:
            </Text>
            <Text>
              When you use our website we may – ourselves or through our
              partners –- collect information such as:
            </Text>
            <Paragraph margin="xs">
              <Text dot>IP address</Text>
              <Text dot>device details</Text>
              <Text dot>
                information about each visit you make to our site (such as page
                response times and length of visit)
              </Text>
              <Text dot>location data</Text>
              <Text dot>
                information about your use of the website through tracking tools
              </Text>
            </Paragraph>
            <Paragraph margin="xs">
              <Text dot>
                For our website: Yolt Technology Services uses Google Analytics
                on our website. We don’t use Google Analytics on our app or on
                your transaction data.
              </Text>
              <Text dot>
                If you don’t want this, you can always disable it through the
                Cookie Settings on the website.
              </Text>
            </Paragraph>
            <Text>
              We use tools to improve the user experience of our website and to
              personalise your experience. Therefore, we perform statistical
              analyses about the way you use the services provided by Yolt
              Technology Services (such as information on how you navigate, how
              much time you spend on pages, how long you visit, and from where
              you came to our service).
            </Text>
          </Paragraph>
          <Title>WHAT DO WE USE YOUR PERSONAL DATA FOR?</Title>
          <Paragraph margin="md">
            <Text>
              Contractual performance: If you want to use our website and have
              the best experience in getting to know our services through our
              website, or for us to deliver you our services or contact you, we
              can only perform these services if we can process your personal
              data for this purpose.
            </Text>
            <Text>
              Legitimate interest: We use your personal data on the basis of our
              legitimate interest and to your benefit so that we can:
            </Text>
            <Paragraph margin="xs">
              <Text dot>
                We can deliver you the benefit of, and connect you to, our
                services
              </Text>
              <Text dot>
                Provide you with updates about Yolt and Yolt Technology Services
              </Text>
              <Text dot>
                Assess the use of our website and contact -points for Yolt
                Technology Services;
              </Text>
              <Text dot>
                Make a secure connection between your device and our website
              </Text>
              <Text dot>
                Track and examine the use of the website to prepare reports on
                its activities and analyse that data
              </Text>
              <Text dot>
                Perform research and trend analysis to optimise your experience;
              </Text>
              <Text dot>
                Creating content using some personal data, which will enable us
                to engage with you in a relevant way.
              </Text>
              <Text dot>
                Reusing and prefilling data you have provided us with before
              </Text>
              <Text dot>
                Re-engaging users and visitors via retargeting practices on the
                website and social media.
              </Text>
              <Text dot>
                We could use your personal data in an anonymised and aggregated
                form to enrich content for:
              </Text>
              <Paragraph margin="xs">
                <Text dot dotColor="gray.500">
                  Infographics
                </Text>
                <Text dot dotColor="gray.500">
                  Sales pitches and propositions
                </Text>
                <Text dot dotColor="gray.500">
                  Social media posts
                </Text>
                <Text dot dotColor="gray.500">
                  Campaigns
                </Text>
              </Paragraph>
            </Paragraph>
            <Text>
              Legal obligations: We may also process your personal data because
              we are under a legal obligation to do so to:
            </Text>
            <Paragraph margin="md">
              <Text dot>
                Prevent and detect fraud, other crime, and security issues and
                to reduce Yolt’s risks for Yolt Technology Services;
              </Text>
              <Text dot>
                Comply with laws and regulations, as well as any sector-specific
                guidelines and regulations.
              </Text>
            </Paragraph>
          </Paragraph>
          <Title>WHERE DO WE STORE YOUR PERSONAL DATA?</Title>
          <Paragraph margin="md">
            <Text>
              Your data is stored in Europe: The personal data we collect from
              you is stored on secure information technology systems located in
              the European Economic Area (for example: The Netherlands, Germany,
              United Kingdom, and Ireland).
            </Text>
          </Paragraph>
          <Title>YOUR KEY RIGHTS</Title>
          <Paragraph margin="md">
            <Text>
              Your primary right is the right to stop us from processing your
              data (right to object): You can do this by sending us an email
              with the request to be forgotten. This will not invalidate any
              processing of the personal data prior to your withdrawal of
              consent.
            </Text>
            <Text>Your other rights are:</Text>
            <Text>
              Right of access and data portability: You can request a copy of
              all personal data you have provided us at any time.
            </Text>
            <Text>
              Right to rectification: You have the right to ask us to rectify
              inaccurate or incomplete personal data which we have about you.
            </Text>
            <Text>
              Right to erasure: You have the right to ask us to erase your
              personal data.
            </Text>
            <Text>
              Right to object: You have the right to object, on grounds relating
              to your particular situation and, at any time, to processing of
              your personal data. If you want to invoke this right, please
              contact us and inform us on your objections.
            </Text>
            <Text>
              Right to object to automatic processing: You don’t need to object
              because we don’t subject you to decisions that significantly
              affect you and are based solely on automated processing which
              significantly affect you.
            </Text>
          </Paragraph>
          <Title>PROTECTING YOUR PERSONAL DATA</Title>
          <Paragraph margin="md">
            <Text>Security is in our DNA:</Text>
            <Text>
              We are committed to ensuring that your personal data is secure.
            </Text>
            <Paragraph margin="xs">
              <Text dot>
                In order to prevent unauthorised access to or disclosure of your
                personal data, we have put in place suitable physical,
                electronic and operational procedures to safeguard and secure
                the personal data we collect about you.
              </Text>
              <Text dot>
                In particular, we protect your personal data by deploying SSL
                and high- standard encryption algorithms.
              </Text>
              <Text dot>
                We also ensure that we meet security standards imposed by law
                that are applicable to the operation of Yolt Technology
                Services.
              </Text>
            </Paragraph>
          </Paragraph>
          <Title>HOW LONG DO WE KEEP YOUR DATA</Title>
          <Paragraph margin="md">
            <Text dot>
              We are only allowed to keep your personal data for as long as it's
              still is necessary for the purpose we initially required it. After
              this we will delete your data.
            </Text>
            <Text dot>
              However, as we are in financial services and bound by strict
              regulations, this means that we need to retain the data we hold
              about you for a minimum of 7 years after termination of your Yolt
              Technology Services account. If (local) regulations require us to
              retain it longer or delete it sooner, we will follow these
              retention periods.
            </Text>
            <Text dot>
              If you invoke your right to be forgotten, we will use feasible
              solutions to make it your personal data no longer directly
              available in our systems, like for instance by archiving it. This
              means that in such case we will no longer process your data.
            </Text>
          </Paragraph>
          <Title>OUR CONTACT DETAILS</Title>
          <Paragraph margin="md">
            <Text dot>
              Yolt and Yolt Technology Services are registered trademarks of
              Yolt Group B.V.
            </Text>
            <Text dot>
              The YTS services in the UK are provided by Yolt Technology
              Services Limited (“YTS Limited”).
            </Text>
            <Text dot>
              YTS Limited’s registered office is a 8-10 Moorgate, London EC2R
              6DA, United Kingdom, company number 12388678.
            </Text>
            <Text dot>
              The YTS services in the EU are provided by Yolt Technology
              Services B.V. (“YTS BV”)
            </Text>
            <Text dot>
              YTS B.V’s registered office is at Hoogoorddreef 60, 1101 BE
              Amsterdam, The Netherlands, company number 76904814.
            </Text>
            <Text dot>
              The regulated Account Information & Payment Initiation services
              (“TPP Functions”) are provided in the European Union by ING Bank
              N.V. (“ING”).
            </Text>
            <Text dot>
              ING has its statutory seat in Amsterdam at Bijlmerplein 888, 1102
              MG, The Netherlands, Trade Register number 33031431.
            </Text>
            <Text dot>
              You can exercise any of the rights under this privacy policy or
              contact us at:
            </Text>
            <Text color="yolt.heroBlue" dot>
              <Link color="yolt.heroBlue" href="mailto:yts@yolt.com">
                yts@yolt.com
              </Link>
            </Text>
            <Text dot>You can contact our Data Protection Officer via:</Text>
            <Text color="yolt.heroBlue" dot>
              <Link color="yolt.heroBlue" href="mailto:dpo@yolt.com">
                dpo@yolt.com
              </Link>
            </Text>
          </Paragraph>
          <Title>COMPLAINTS TO THE PRIVACY REGULATOR</Title>
          <Paragraph margin="md">
            <Text>
              You have the right to complain to the privacy regulator in the
              country in which you reside, where you work, or anywhere where you
              believe we might have broken data protection rules.
            </Text>
            <Text>
              In the UK, the privacy regulator is the Information Commissioner's
              Office (the "ICO"). The ICO can be contacted at:
            </Text>
            <Paragraph margin="xs">
              <Text dot>
                Address: Information Commissioner's Office, Wycliffe House,
                Water Lane, Wilmslow, Cheshire, SK9 5AF
              </Text>
              <Text dot>
                Telephone: 0303 123 1113 (local rate) or 01625 545 745
              </Text>
              <Text dot>
                Email:{" "}
                <Link href="https://ico.org.uk/global/contact-us">
                  https://ico.org.uk/global/contact-us
                </Link>
              </Text>
            </Paragraph>
          </Paragraph>
        </Stack>
      </Container>
    </>
  );
};

export default PrivacyStatement;
