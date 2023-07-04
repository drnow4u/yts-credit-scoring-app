import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router";

import { useGetClient } from "api/management/clients";

import Loading from "components/Loading";

import Accounts from "./components/Accounts";
import AdditionalInfo from "./components/AdditionalInfo";
import EmailTemplate from "./components/EmailTemplate";
import FeatureToogle from "./components/FeatureToogle";
import Header from "./components/Header";
import LogoUpload from "./components/LogoUpload";

const { Flex, Tabs, TabList, Tab, TabPanels, TabPanel, Text } = Chakra;

export default function ManagementClientDetails() {
  const { t } = useTranslation();
  const { clientId } = useParams<{ clientId: string }>();
  const { data } = useGetClient(clientId);

  if (!data || !clientId) {
    return (
      <Loading>
        <Text>{t(`management.loadingClient`)}</Text>
      </Loading>
    );
  }

  return (
    <Flex w={{ sm: "90%", xl: "95%" }} direction="column">
      <Header logo={data.logo} name={data.name} />
      <Tabs isFitted variant="enclosed">
        <TabList mb="1em">
          <Tab>General</Tab>
          <Tab>Accounts</Tab>
          <Tab>Email</Tab>
        </TabList>
        <TabPanels>
          <TabPanel>
            <Flex>
              <Flex
                flexDirection={{
                  md: "row",
                  xl: "column",
                }}
                justifyContent="space-between"
                flexWrap="wrap"
              >
                <FeatureToogle />
                <LogoUpload clientId={clientId} />
              </Flex>
              <AdditionalInfo
                redirectUrl={data.redirectUrl}
                additionalConsentText={data.additionalConsentText}
                additionalReportText={data.additionalReportText}
              />
            </Flex>
          </TabPanel>
          <TabPanel>
            <Accounts />
          </TabPanel>
          <TabPanel>
            <EmailTemplate />
          </TabPanel>
        </TabPanels>
      </Tabs>
    </Flex>
  );
}
