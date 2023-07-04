import { Chakra } from "@yolt/design-system";

import { HOMEPAGE } from "helpers/constants";

import { Text, Title } from "./components";

const {
  Heading,
  Stack,
  Image,
  Box,
  Flex,
  Container,
  Link,
  Divider,
  Text: TextLink,
} = Chakra;

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
            Legal
          </Heading>
          <Divider backgroundColor="yolt.heroBlue" />
          <Title>
            Use of the Yolt websites&nbsp;
            <TextLink as="a" href="https://www.yolt.com">
              https://www.yolt.com
            </TextLink>
            &nbsp;and{" "}
            <TextLink as="a" href="https://developer.yolt.com">
              {" "}
              https://developer.yolt.com
            </TextLink>{" "}
            ("website" below) is subject to:
          </Title>
          <Text dot>
            {" "}
            the{" "}
            <TextLink
              as="a"
              color="yolt.heroBlue"
              href="https://www.yolt.com/about-us/terms-conditions"
            >
              terms and conditions
            </TextLink>
            , and{" "}
          </Text>
          <Text dot>
            {" "}
            the website{" "}
            <TextLink
              as="a"
              color="yolt.heroBlue"
              href="/admin/privacy-statement"
            >
              privacy policy.
            </TextLink>
          </Text>
          <Text>
            By accessing and using this website, you (also "user" below) agree
            to be bound by the terms and conditions below and the website
            privacy policy. Please review them carefully. If you do not agree to
            the terms and conditions or website privacy policy then you must not
            use the website.
          </Text>
          <Text>
            If you wish to read the Yolt Terms & Conditions and Privacy Policy
            that are applicable to the Account Information Services and/or
            Payment Initiation Services that are made available to you as part
            of the service of our client that you are using, please visit:{" "}
            <TextLink
              as="a"
              color="yolt.heroBlue"
              href="https://www.yolt.com/about-us/terms-conditions"
            >
              yolt.com/about-us/terms-conditions.
            </TextLink>
          </Text>
          <Title>Use of website</Title>
          <Text>
            The information shown on this website has been put together by Yolt
            with due care, but no guarantees can be given of its accuracy or
            completeness. On this website, Yolt provides information purely
            about (financial) products and services that are provided by Yolt.
            Yolt reserves the right to make changes to the website without prior
            notice. Neither this website nor the websites or documents to which
            this website provides access contain an invitation to buy or sell
            securities or other financial products or services. The information
            offered is not intended to take the place of professional advice.
            Without verification or further advice, use of the information
            presented is at the user's own expense and risk. You should use your
            own judgment, and seek professional advice if appropriate.
          </Text>
          <Text>
            We may suspend or end your use of the website at any time with or
            without informing you. We have no obligation to resume provision of
            the website to you, if suspended or closed.
          </Text>
          <Title>Information from third parties, products and services</Title>
          <Text>
            Information supplied by third parties, references or hyperlinks to
            other websites which are not the property of Yolt are only included
            for information of the user of this website. Although Yolt is
            extremely selective concerning the third-party information included
            on this website or the sites to which reference is made, it does not
            endorse their content or functioning, or the quality of any products
            and/or services they offer. Yolt gives no guarantee whatsoever and
            accepts no liability whatsoever in relation to the content of such
            websites.
          </Text>
          <Title>Data protection</Title>
          <Text>
            Any personal information you supply to us (and which we collect from
            you) when using this website will be used by us in accordance with
            our website{" "}
            <TextLink as="a" href="/admin/privacy-statement">
              privacy policy.
            </TextLink>
          </Text>
          <Title>Intellectual property rights</Title>
          <Text>
            All information displayed on this website, including texts, photos,
            illustrations, graphic material, (trade) names, logos, product and
            service marks, are the property of or licensed to Yolt and are
            protected by database rights, copyrights, trade mark rights and/or
            any other intellectual property rights. The (intellectual) property
            rights in no way transfer to legal entities or persons who obtain
            access to this website. The contents of this website may only be
            used for personal, non-commercial purposes. The user of this website
            is not permitted to reproduce, forward, distribute or disseminate
            the content of this website or make it available to third parties in
            exchange for compensation, without prior written permission from
            Yolt. Where Yolt has included one or more social sharing buttons
            next to content on this website, users may use those buttons to
            forward the relevant content for their personal, non-commercial
            purposes.
          </Text>
          <Title>No liability</Title>
          <Text>
            Yolt shall have no liability to you for any damage resulting from
            accessing or using this website, and does not warrant that the
            website (or any content displayed on the website) is or shall be
            error-free, or that it shall function uninterrupted. Although Yolt
            makes every effort to keep the content, information and materials
            displayed on this website as up to date and complete as possible, it
            cannot give any guarantee for the accuracy, validity, completeness
            or currency of the information which is published on this website or
            to which this website gives access. Yolt accepts no liability
            whatsoever in that respect.
          </Text>
          <Title>Applicable law</Title>
          <Text>
            Dutch law applies to these terms and conditions, their subject
            matter and their formation. You and we both agree that the courts of
            Amsterdam, The Netherlands will have exclusive jurisdiction over
            disputes which could not be resolved amicably.
          </Text>
          <Title>Amendments</Title>
          <Text>
            Yolt reserves the right to change these terms and conditions at any
            time. You should check this page regularly for any changes.
          </Text>
          <Title>Complaints</Title>
          <Text>
            Yolt appreciates it greatly if you notify us of a question, problem
            or complaint, because this gives Yolt the opportunity to seek a
            suitable solution. It also gives Yolt the opportunity to improve its
            service to you and other customers. Please contact us using the
            details below.
          </Text>
          <Title>Yolt details</Title>
          <TextLink
            fontWeight="700"
            color="yolt.heroBlue"
            as="a"
            href="https://www.yolt.com/"
          >
            https://www.yolt.com/
          </TextLink>
          <TextLink
            fontWeight="700"
            color="yolt.heroBlue"
            as="a"
            href="https://www.yolt.com/about-us/contact-us"
          >
            Contact us
          </TextLink>
          <TextLink
            fontWeight="700"
            color="yolt.heroBlue"
            as="a"
            href="https://www.yolt.com/complaints"
          >
            Make a complaint
          </TextLink>
          <Text>Yolt and YTS are registered trademarks of Yolt Group BV.</Text>
          <Text>
            The Yolt services in the EU/EEA are provided by Yolt Technology
            Services B.V.
          </Text>
          <Text>
            Yolt Technology Services B.V. is regulated and licensed by De
            Nederlandsche Bank ('DNB').
          </Text>
          <Text>
            You can find details on Yolt at
            <TextLink as="a" href="https://www.dnb.nl/openbaar-register">
              www.dnb.nl/openbaar-register
            </TextLink>
            (reference number: R179712 or searching for “Yolt Technology
            Services”). Yolt passports its regulatory permissions into the
            European Economic Area (EEA). Passporting allows a firm registered
            in the EEA to do business in any other EEA state without the need
            for further authorization from that country.
          </Text>
          <Text>
            Yolt Technology Services BV. has its statutory seat in Amsterdam at
            Hoogoorddreef 60, 1101 BE Amsterdam, the Netherlands and is entered
            in the Amsterdam Trade Register under no. 76904814.
          </Text>
          <Text>
            The Yolt services in the UK are provided by Yolt Technology Services
            Limited. Yolt Technology Services Limited is authorised by the
            Financial Conduct Authority under the Payment Service Regulations
            2017. You can find details on Yolt at register.fca.org.uk (reference
            number: 921127).
          </Text>
          <Text>
            Yolt Technology Services Limited’s registered office is a 8-10
            Moorgate, London EC2R 6DA, United Kingdom, company number 12388678.
          </Text>
        </Stack>
      </Container>
    </>
  );
};

export default PrivacyStatement;
