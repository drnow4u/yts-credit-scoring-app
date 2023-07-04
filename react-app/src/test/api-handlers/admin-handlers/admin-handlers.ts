import { DateTime } from "luxon";
import { rest } from "msw";

import { ADMIN_TOKEN_STORAGE_NAME } from "helpers/constants";

import { getAccount } from "test/data/admin/account";
import { createInvitationTemplateOptions } from "test/data/admin/invitation-template-option";
import { getMetrics } from "test/data/admin/metrics";
import { getToken } from "test/data/admin/token";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { adminBase, IS_ADMIN_AUTHENTICATED_KEY } from "./common";
import { creditReportHandlers } from "./credit-report-handlers";
import { tokenHandlers } from "./token-handlers";
import { usersHandlers } from "./users-handlers";

const adminHandlers = [
  rest.get(`${adminBase}/account`, async (req, res, ctx) => {
    try {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      const account = getAccount();
      return res(ctx.json(account));
    } catch (error) {
      return res(ctx.status(401));
    }
  }),

  rest.get(`${adminBase}/oauth2/callback/github`, async (req, res, ctx) => {
    try {
      const account = getToken();
      return res(ctx.json(account));
    } catch (error) {
      return res(ctx.status(401));
    }
  }),

  rest.put(`${adminBase}/logout`, async (req, res, ctx) => {
    sessionStorage.removeItem(IS_ADMIN_AUTHENTICATED_KEY);
    sessionStorage.removeItem(ADMIN_TOKEN_STORAGE_NAME);
    return res(ctx.status(200));
  }),

  rest.post(`${adminBase}/log/signature`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.status(200));
  }),

  rest.get(`${adminBase}/client/template`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }
    return res(ctx.json(createInvitationTemplateOptions(3)));
  }),

  rest.get(`${adminBase}/metrics/years`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }
    const currentYear = DateTime.now().year;
    const years = Array.from({ length: 5 }, (_, i) => currentYear - 4 + i);
    return res(ctx.json(years));
  }),

  rest.get(`${adminBase}/metrics`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }
    const year = parseInt(req.url.searchParams.get("year") ?? "", 10);
    return res(ctx.json(getMetrics(year)));
  }),
];

adminHandlers.push(...usersHandlers, ...tokenHandlers, ...creditReportHandlers);

export { adminBase, adminHandlers, IS_ADMIN_AUTHENTICATED_KEY };
