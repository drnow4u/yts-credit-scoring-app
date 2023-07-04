import faker from "@faker-js/faker";
import { rest } from "msw";

import { connectSiteRedirectUrl, getSites } from "test/data/site";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { IS_USER_AUTHENTICATED_KEY, publicBase } from "./common";

import type { ConnectSiteResponse } from "types/site";

const sitesHandlers = [
  rest.get(`${publicBase}/sites`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const sites = getSites();

    return res(ctx.json(sites));
  }),

  rest.post(`${publicBase}/sites/connect`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    const response: ConnectSiteResponse = {
      redirectUrl: connectSiteRedirectUrl,
    };
    return res(ctx.json(response));
  }),

  rest.post(`${publicBase}/sites/user-site`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.json({ activityId: faker.datatype.uuid() }));
  }),
];

export { sitesHandlers };
