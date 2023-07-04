import { CreditScoreResponse, ShareReportResponse } from "types/credit-report";

import { checkPollingStatus } from "helpers/api";
import { PublicHTTPClient } from "helpers/HTTPClient";

import type { Account } from "types/account";
import type { ClientResponse } from "types/client";
import type { Consent } from "types/consent";
import type { PrivacyPolicy } from "types/privacy-policy";
import type {
  ConnectSiteResponse,
  CreateUserSiteResponse,
  Site,
} from "types/site";
import type { TermsConditions } from "types/terms-conditions";
import type { TokenResponse } from "types/token";

class CreditScoring {
  getToken(userHash: string) {
    return PublicHTTPClient.get<TokenResponse>(`token/${userHash}`);
  }

  getClient() {
    return PublicHTTPClient.get<ClientResponse>("client");
  }

  sendConsent(consent: Consent) {
    return PublicHTTPClient.post<void>("consent", consent);
  }

  getTermsConditions() {
    return PublicHTTPClient.get<TermsConditions>(
      "legal-document/terms-conditions"
    );
  }

  getPrivacyPolicy() {
    return PublicHTTPClient.get<PrivacyPolicy>("legal-document/privacy-policy");
  }

  getSites() {
    return PublicHTTPClient.get<Site[]>("sites");
  }

  connectSite(siteId: Site["id"]) {
    return PublicHTTPClient.post<ConnectSiteResponse>("sites/connect", {
      siteId,
    });
  }
  connectSiteCallback(url: string) {
    return PublicHTTPClient.post<CreateUserSiteResponse>("sites/user-site", {
      url,
    });
  }

  getAccounts() {
    return PublicHTTPClient.get<Account[]>("accounts", {
      validateStatus: checkPollingStatus,
    });
  }
  selectAccount(id: Account["id"]) {
    return PublicHTTPClient.post<void>("accounts/select", { id });
  }

  getCreditReport() {
    return PublicHTTPClient.get<CreditScoreResponse>("cashflow-overview", {
      validateStatus: checkPollingStatus,
    });
  }

  confirmShareReport() {
    return PublicHTTPClient.post<ShareReportResponse>(
      "cashflow-overview/confirm"
    );
  }

  refuseShareReport() {
    return PublicHTTPClient.post<ShareReportResponse>(
      "cashflow-overview/refuse"
    );
  }
}

export const PublicCreditScoringAPI = new CreditScoring();
