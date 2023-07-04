export type ClientSettings = {
  defaultLanguage: string;
  categoryFeatureToggle: boolean;
  monthsFeatureToggle: boolean;
  overviewFeatureToggle: boolean;
  pdscoreFeatureToggle: boolean;
  signatureVerificationFeatureToggle: boolean;
  apiTokenFeatureToggle: boolean;
};

export interface Account {
  email: string;
  name: string;
  clientSettings: ClientSettings;
}
