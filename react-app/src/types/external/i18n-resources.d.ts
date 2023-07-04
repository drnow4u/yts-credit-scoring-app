import translationEN from "i18n/en/translation.json";
import translationNL from "i18n/nl/translation.json";

import "react-i18next";

declare module "react-i18next" {
  interface Resources {
    nl: typeof translationNL;
    en: typeof translationEN;
  }
}

declare module "react-i18next" {
  interface CustomTypeOptions {
    defaultNS: "nl";
    resources: {
      en: typeof import("i18n/en/translation.json");
      nl: typeof import("i18n/nl/translation.json");
    };
  }
}
