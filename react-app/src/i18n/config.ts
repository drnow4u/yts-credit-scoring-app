import translationEN from "i18n/en/translation.json";
import translationFR from "i18n/fr/translation.json";
import translationNL from "i18n/nl/translation.json";
import i18n from "i18next";
import { initReactI18next } from "react-i18next";

export const resources = {
  en: {
    translation: translationEN,
  },
  nl: {
    translation: translationNL,
  },
  fr: {
    translation: translationFR,
  },
} as const;

i18n.use(initReactI18next).init({
  lng: "nl",
  fallbackLng: ["nl", "en", "fr"],
  resources,
  supportedLngs: ["en", "nl", "fr"],
  lowerCaseLng: true,
});

export default i18n;
