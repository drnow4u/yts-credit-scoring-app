import { ClientSettings } from "types/admin/account";

export const parseFeatureKeys = (
  accountKeys: Omit<ClientSettings, "defaultLanguage" | "clientName">
) => ({
  categories: accountKeys.categoryFeatureToggle,
  months: accountKeys.monthsFeatureToggle,
  overview: accountKeys.overviewFeatureToggle,
  apiToken: accountKeys.apiTokenFeatureToggle,
  signatureVerification: accountKeys.signatureVerificationFeatureToggle,
  pDScoreFeature: accountKeys.pdscoreFeatureToggle,
});
export default parseFeatureKeys;
