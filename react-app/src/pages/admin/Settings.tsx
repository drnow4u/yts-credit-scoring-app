import { lazy } from "react";

import { Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

const { Heading, Stack } = Chakra;

const ApiSettingsTable = lazy(
  () => import("components/ApiSettingsTable/ApiSettingsTable")
);

function Settings() {
  const { t } = useTranslation();

  return (
    <Stack as="main" display="flex" padding="2rem" spacing="2rem">
      <Heading textColor="yolt.heroBlue" fontWeight={500} fontSize="lg" as="h1">
        {t("settings.title")}
      </Heading>

      <ApiSettingsTable />
    </Stack>
  );
}

export default Settings;
