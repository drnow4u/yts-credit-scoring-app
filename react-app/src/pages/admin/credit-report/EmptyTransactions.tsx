import { Button, Chakra } from "@yolt/design-system";
import { ReactComponent as MidnightDoubloons } from "icons/midnight_doubloons.svg";
import { useTranslation } from "react-i18next";

const { Box, Heading, Stack, Link } = Chakra;

export const EmptyTransactions = () => {
  const { t } = useTranslation();

  return (
    <Box padding="32px" width="100%">
      <Stack spacing="8" width="100%">
        <Box display="flex" alignSelf="center" flexDirection="column">
          <Box display="flex" alignSelf="center">
            <MidnightDoubloons height={100} width={100} />
          </Box>
          <Heading
            textAlign="center"
            as="h1"
            fontSize="1.4rem"
            fontWeight="700"
          >
            {t("report.emptyTransactions.notGenerededReport")}
          </Heading>
          <Heading
            marginTop={5}
            as="h2"
            width="100%"
            textAlign="center"
            color="black"
          >
            {t("report.emptyTransactions.accountNotEnoughTransactions")}
          </Heading>
          <Heading
            marginTop={2}
            as="h2"
            width="100%"
            textAlign="center"
            color="black"
          >
            {t("report.emptyTransactions.contactCustomer")}
          </Heading>
          <Link
            marginTop={4}
            alignSelf="center"
            textDecor="none"
            href="/admin/dashboard"
          >
            <Button>{t("report.emptyTransactions.buttonBackDashboard")}</Button>
          </Link>
        </Box>
      </Stack>
    </Box>
  );
};

export default EmptyTransactions;
