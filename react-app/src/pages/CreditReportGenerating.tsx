import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { Center, Heading, Stack, Text } = Chakra;

function CreditReportGenerating() {
  const { t } = useTranslation();

  return (
    <Center as="main" display="flex" height="100%">
      <Stack
        display="flex"
        spacing="2rem"
        alignItems="center"
        justifyContent="center"
        padding="16px"
        margin="16px"
        bg="white"
      >
        <Heading as="h1">{t("report.processing")}</Heading>
        <Heading fontSize="1.3rem" fontWeight="300">
          {t("report.generating.part1")}
        </Heading>

        <Text>{t("report.generating.part2")}</Text>
        <Text>{t("report.generating.part3")}</Text>
      </Stack>
    </Center>
  );
}

export default CreditReportGenerating;
