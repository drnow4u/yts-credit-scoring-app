import { rest } from "msw";

import { getAccounts } from "test/data/account";
import { getClient } from "test/data/client";
import { getPrivacyPolicy } from "test/data/privacy-policy";
import { getTermsConditions } from "test/data/terms-conditions";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { cashflowHandlers } from "./cashflow-overview-handlers";
import { IS_USER_AUTHENTICATED_KEY, publicBase } from "./common";
import { sitesHandlers } from "./sites-handlers";

import type { Consent } from "types/consent";
import type { TokenResponse } from "types/token";

const publicHandlers = [
  rest.get(`${publicBase}/token/:hash`, async (req, res, ctx) => {
    const { hash } = req.params as { hash?: string };
    if (!hash || hash === "undefined") {
      return res(ctx.status(401));
    }

    sessionStorage.setItem(IS_USER_AUTHENTICATED_KEY, "true");

    const response: TokenResponse = {
      token: "Bearer JWT_TOKEN_HERE",
    };

    return res(ctx.json(response));
  }),

  rest.get(`${publicBase}/client`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const client = getClient();

    return res(ctx.json(client));
  }),

  rest.get(
    `${publicBase}/legal-document/terms-conditions`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_USER_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      const termsConditions = getTermsConditions();

      return res(ctx.json(termsConditions));
    }
  ),

  rest.get(
    `${publicBase}/legal-document/privacy-policy`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_USER_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      const privacyPolicy = getPrivacyPolicy();

      return res(ctx.json(privacyPolicy));
    }
  ),

  rest.post(`${publicBase}/consent`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const { consent } = req.body as Consent;
    if (consent === false) {
      return res(ctx.status(200));
    }
    return res(ctx.status(204));
  }),

  rest.get(`${publicBase}/accounts`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    if (process.env.NODE_ENV === "development") {
      if (Math.random() > 0.5) {
        return res(ctx.status(202));
      }
    }

    const accounts = getAccounts();

    return res(ctx.json(accounts));
  }),
  rest.post(`${publicBase}/accounts/select`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.status(202));
  }),
];

publicHandlers.push(...sitesHandlers, ...cashflowHandlers);

export { IS_USER_AUTHENTICATED_KEY, publicBase, publicHandlers };
