import { Button, Chakra } from "@yolt/design-system";
import { useTranslation } from "react-i18next";

import { USER_LANGUAGE_NAME } from "../helpers/constants";

const { Box, ButtonGroup, Text } = Chakra;

function LanguageSelect() {
  const { t, i18n } = useTranslation();
  const changeLanguage = (language: string) => {
    i18n.changeLanguage(language);
    localStorage.setItem(USER_LANGUAGE_NAME, language);
  };

  return (
    <Box>
      <Text marginRight={1} as="span">
        {t("languageSelection")}:
      </Text>
      <ButtonGroup spacing={2}>
        <Button variant="link" onClick={() => changeLanguage("en")}>
          English
        </Button>
        <Button variant="link" onClick={() => changeLanguage("nl")}>
          Nederlands
        </Button>
        <Button variant="link" onClick={() => changeLanguage("fr")}>
          French
        </Button>
      </ButtonGroup>
    </Box>
  );
}

export default LanguageSelect;
