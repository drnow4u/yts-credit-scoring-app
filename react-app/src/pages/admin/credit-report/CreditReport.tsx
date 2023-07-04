import { Chakra } from "@yolt/design-system";
import { ReactComponent as CategoryIcon } from "icons/category-generic.svg";
import { ReactComponent as DocumentIcon } from "icons/document-icon.svg";
import { ReactComponent as EyeOpenIcon } from "icons/eye-icon-open.svg";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";
import { Navigate } from "react-router-dom";

import {
  CreditReportCategoriesTables,
  CreditReportDashboard,
  CreditReportOverview,
  CreditReportTable,
} from "components/creditReport";
import {
  FeatureToggle,
  useFeatureToggle,
} from "components/FeatureToggle.provider";
import Loading from "components/Loading";
import SignatureInfo from "components/SignatureInfo";

import { useRedirect } from "utils/route-utils";

import { useCreditReport } from "./context/useCreditReport";
import DownloadReportButton from "./DownloadReportButton";
import EmptyTransactions from "./EmptyTransactions";

const {
  Box,
  Heading,
  Stack,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
  Text,
  Flex,
  Button,
} = Chakra;

function CreditReportPage() {
  const { t } = useTranslation();
  const [{ months, overview, categories, signatureVerification }] =
    useFeatureToggle();

  const { userId } = useParams<{ userId: string }>();
  const {
    creditReportData: creditReport,
    isFetchingCreditReport,
    creditReportOverviewData: creditReportOverview,
    isFetchingOverview,
    creditReportMonthlyData: creditReportMonthly,
    isFetchingMonths,
  } = useCreditReport();
  const redirectToDashboard = useRedirect("/admin");

  if (
    !creditReport ||
    (!creditReportMonthly && months) ||
    (!creditReportOverview && overview) ||
    isFetchingOverview ||
    isFetchingCreditReport ||
    isFetchingMonths
  ) {
    return <Loading />;
  }

  if (!userId) {
    return <Navigate to="admin" replace />;
  }

  const generate = async () => {
    const newWindow = window.open(`${window.location.href}/download`);

    if (newWindow) newWindow.opener = null;
  };

  const isTransactionsExsist = creditReport.adminReport.transactionsSize !== 0;

  return (
    <Stack as="main">
      <Stack
        direction="row"
        display="flex"
        alignItems="flex-start"
        w="100%"
        padding="40px"
        bg="white"
      >
        <Stack width="100%" spacing={4}>
          <Flex width="100%">
            <Box width="100%" display="inline-block">
              <Heading display="" as="h1">
                <Text
                  maxWidth={24}
                  onClick={redirectToDashboard}
                  _hover={{ cursor: "pointer" }}
                >
                  {t("creditReport.details")}
                </Text>
              </Heading>
            </Box>
            <Flex marginLeft="auto" display="inline-block">
              <FeatureToggle toggle="months">
                <Button isDisabled={!isTransactionsExsist} onClick={generate}>
                  {t("creditReport.generatePdf")}
                </Button>
              </FeatureToggle>
            </Flex>
          </Flex>
          <CreditReportOverview
            creditReport={creditReport.adminReport}
            userEmail={creditReport.userEmail}
          />
        </Stack>
        <Box>
          <FeatureToggle toggle="months">
            <DownloadReportButton disabled={!isTransactionsExsist} />
          </FeatureToggle>
        </Box>
      </Stack>
      {isTransactionsExsist ? (
        <Box padding="16px" width="100%">
          <Tabs variant="buttons">
            <aside>
              <TabList
                sx={{
                  "button[aria-selected='true'] path": {
                    fill: "yolt.primaryBlue",
                  },
                  "button path": {
                    fill: "yolt.lightGrey",
                  },
                }}
              >
                <FeatureToggle toggle="overview">
                  <Tab>
                    <Stack
                      direction="row"
                      justify="flex-start"
                      alignItems="center"
                      w="100%"
                    >
                      <EyeOpenIcon /> <Text>{t("creditReport.overview")}</Text>
                    </Stack>
                  </Tab>
                </FeatureToggle>
                <FeatureToggle toggle="months">
                  <Tab>
                    <Stack
                      direction="row"
                      justify="flex-start"
                      alignItems="center"
                      w="100%"
                    >
                      <DocumentIcon /> <Text>{t("general.months")}</Text>
                    </Stack>
                  </Tab>
                </FeatureToggle>
                <FeatureToggle toggle="categories">
                  <Tab>
                    <Stack
                      direction="row"
                      justify="flex-start"
                      alignItems="center"
                      w="100%"
                    >
                      <CategoryIcon /> <Text>{t("general.categories")}</Text>
                    </Stack>
                  </Tab>
                </FeatureToggle>
              </TabList>
            </aside>
            <Box>
              <TabPanels>
                {overview &&
                  months &&
                  creditReportMonthly &&
                  creditReportOverview && (
                    <TabPanel>
                      <Stack spacing="8" width="100%">
                        <Text
                          as="h1"
                          isTruncated
                          fontSize="xl"
                          fontWeight="600"
                          textColor="yolt.heroBlue"
                        >
                          {t("creditReport.overview")}
                        </Text>
                        <CreditReportDashboard
                          creditScoreMonthly={creditReportMonthly}
                          creditScoreOverviewResponse={creditReportOverview}
                          creditScoreResponse={creditReport}
                        />
                      </Stack>
                    </TabPanel>
                  )}
                {months && creditReportMonthly && (
                  <TabPanel>
                    <Stack spacing="8" width="100%">
                      <Text
                        as="h1"
                        isTruncated
                        fontSize="xl"
                        fontWeight="600"
                        textColor="yolt.heroBlue"
                      >
                        {t("general.months")}
                      </Text>
                      <CreditReportTable
                        creditScoreMonthly={creditReportMonthly}
                        creditReport={creditReport.adminReport}
                      />
                    </Stack>
                  </TabPanel>
                )}
                {categories && (
                  <TabPanel>
                    <Stack spacing="8" width="100%">
                      <Text
                        as="h1"
                        isTruncated
                        fontSize="xl"
                        fontWeight="600"
                        textColor="yolt.heroBlue"
                      >
                        {t("general.categories")}
                      </Text>
                      <CreditReportCategoriesTables
                        currency={creditReport.adminReport.currency}
                        userId={userId}
                      />
                    </Stack>
                  </TabPanel>
                )}
              </TabPanels>
              {signatureVerification ? (
                <SignatureInfo
                  signature={creditReport.signature}
                  signatureVerified={creditReport.isSignatureVerified}
                />
              ) : null}
            </Box>
          </Tabs>
        </Box>
      ) : (
        <EmptyTransactions />
      )}
    </Stack>
  );
}

export default CreditReportPage;
