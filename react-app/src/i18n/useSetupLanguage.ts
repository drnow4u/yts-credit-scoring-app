import { useEffect } from "react";

import { USER_LANGUAGE_NAME } from "helpers/constants";

import i18n from "./config";

function useSetupLanguage(clientLanguage?: string) {
  const language =
    localStorage.getItem(USER_LANGUAGE_NAME) ?? clientLanguage ?? i18n.language;

  //if there is default language defined for client, and user didn't override it, save it for later to avoid fallback into i18n default on pages where we can't reload client from api
  if (clientLanguage && !localStorage.getItem(USER_LANGUAGE_NAME))
    localStorage.setItem(USER_LANGUAGE_NAME, clientLanguage);

  useEffect(() => {
    i18n.changeLanguage(language);
  }, [language]);
}

export default useSetupLanguage;
