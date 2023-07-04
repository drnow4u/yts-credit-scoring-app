//FIXME: temporary implementation, have to be reviewed when backend will be ready

import { DateTime } from "luxon";

export type ClientCreateDTO = {
  name: string;
  siteTag: string;
  defaultLanguage: string;
  logo?: any;
  additionalConsentText?: string;
  additionalReportText?: string;
  redirectUrl?: string;
  featureToggle?: {
    overview?: boolean;
    months?: boolean;
    categories?: boolean;
    apiToken?: boolean;
    pDScoreFeature?: boolean;
    signatureVerification?: boolean;
  };
};

export interface ClientCreatedResponse extends ClientCreateDTO {
  id: string;
  creationDate: string;
}

export type ClientEmailTemplatesDTO = {
  id: string;
  name: string;
  title: string;
  subTitle: string;
  welcomeBox: string;
  buttonText: string;
  summaryBox: string;
};

export type ClientEmailTemplatesRequest = {
  id: string;
  name: string;
  title: string;
  subTitle: string;
  welcomeBox: string;
  buttonText: string;
  summaryBox: string;
  ticket: string;
  reason: string;
};

export type ClientDetailsDTO = {
  id: string;
  name: string;
  logo: string | null;
  creationDate: string;
  defaultLanguage?: string;
  additionalConsentText?: string;
  additionalReportText?: string;
  redirectUrl?: string;
  featureToggle?: {
    overview?: boolean;
    months?: boolean;
    categories?: boolean;
    apiToken?: boolean;
    pDScoreFeature?: boolean;
    signatureVerification?: boolean;
  };
};

export type AccountDetailsDTO = {
  id: string;
  email: string;
  provider: string;
  creationDate: string;
};

export type AccountCreateDTO = {
  id: string;
  email: string;
  provider: string;
  userName: string;
  ticket: string;
  reason: string;
};

export type AccountDeleteDTO = {
  id: string;
  ticket: string;
  reason: string;
};

export type ClientDetails = Omit<ClientDetailsDTO, "creationDate"> & {
  creationDate: DateTime;
};

export type AccountDetails = Omit<AccountDetailsDTO, "creationDate"> & {
  creationDate: DateTime;
};
export type ClientLogoUpload = {
  logo: any;
};

export type ClientsDetailsResponse = ClientDetailsDTO[];
export type AccountsDetailsResponsee = AccountDetailsDTO[];

export type ClientUpdateDTO = {
  id: string;
  defaultLanguage?: string;
  additionalConsentText?: string;
  additionalReportText?: string;
  redirectUrl?: string;
};

export type ClientUpdateFeatureToggle = {
  id: string;
  overview?: boolean;
  months?: boolean;
  categories?: boolean;
  apiToken?: boolean;
  pDScoreFeature?: boolean;
  signatureVerification?: boolean;
};

export type ClientUpdateFeatureToggleResponse = ClientUpdateFeatureToggleDTO;
