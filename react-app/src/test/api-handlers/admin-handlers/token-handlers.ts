import { rest } from "msw";

import {
  createApiToken,
  getTokenList,
  permissionsTokenList,
} from "test/data/admin/api-key";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { adminBase, IS_ADMIN_AUTHENTICATED_KEY } from "./common";

const tokenHandlers = [
  rest.post(`${adminBase}/token`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.json(createApiToken()));
  }),

  rest.get(`${adminBase}/token/permissions`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.json(permissionsTokenList()));
  }),

  rest.get(`${adminBase}/token`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(ctx.json(getTokenList()));
  }),

  rest.put(`${adminBase}/token/:tokenId`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_ADMIN_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }
    const response = getTokenList()[0];
    response.status = "REVOKE";

    return res(ctx.json(response));
  }),
];

export { tokenHandlers };
