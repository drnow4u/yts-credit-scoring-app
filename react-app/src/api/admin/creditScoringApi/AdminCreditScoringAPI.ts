import { parse } from "query-string";
import { InvitationTemplateOption } from "types/admin/invitation-template-option";
import { MetricsResponse, MetricsYearsResponse } from "types/admin/metrics";
import { Token } from "types/admin/token";

import { AdminHTTPClient } from "helpers/HTTPClient";

import type { Account } from "types/admin/account";

function getAccount() {
  return AdminHTTPClient.get<Account>("account");
}

function getExchangeToken(url: string, providerName: string) {
  const { code, state } = parse(url);
  const params = {
    code: code,
    state: state,
  };
  return AdminHTTPClient.get<Token>(`oauth2/callback/${providerName}`, {
    params,
  });
}

function getInvitationTemplates() {
  return AdminHTTPClient.get<InvitationTemplateOption[]>("client/template");
}

function logout() {
  return AdminHTTPClient.put<void>(`logout`);
}

function getMetricsData(year?: number) {
  const params = year
    ? {
        year: year,
      }
    : undefined;
  return AdminHTTPClient.get<MetricsResponse>("metrics", { params });
}

function getMetricsYears() {
  return AdminHTTPClient.get<MetricsYearsResponse>("metrics/years");
}

export {
  getAccount,
  getExchangeToken,
  getInvitationTemplates,
  getMetricsData,
  getMetricsYears,
  logout,
};
