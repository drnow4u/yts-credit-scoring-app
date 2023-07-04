import { rest } from "msw";

import { createCreditScoreResponse } from "test/data/credit-report";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { IS_USER_AUTHENTICATED_KEY, publicBase } from "./common";

const cashflowHandlers = [
  rest.get(`${publicBase}/cashflow-overview`, async (req, res, ctx) => {
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

    const creditReport = createCreditScoreResponse();

    return res(ctx.json(creditReport));
  }),

  rest.post(
    `${publicBase}/cashflow-overview/confirm`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_USER_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(
        ctx.status(200),
        ctx.json({
          redirectUrl: "https://www.yolt.com/",
        })
      );
    }
  ),

  rest.post(`${publicBase}/cashflow-overview/refuse`, async (req, res, ctx) => {
    const isAuthenticated = !!checkIfAuthenticated(
      IS_USER_AUTHENTICATED_KEY,
      req.headers.get(TOKEN_HEADER_NAME)
    );
    if (!isAuthenticated) {
      return res(ctx.status(401));
    }

    return res(
      ctx.status(200),
      ctx.json({
        redirectUrl: "https://www.yolt.com/",
      })
    );
  }),
];

export { cashflowHandlers };
