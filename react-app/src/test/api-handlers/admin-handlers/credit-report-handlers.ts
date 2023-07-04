import JSZip from "jszip";
import { rest } from "msw";

import { createCreditScoreCategorisedResponse } from "test/data/admin/categories";
import { createCreditScoreAdminResponse } from "test/data/admin/credit-report";
import { createCreditScoreMonthlyAdminResponse } from "test/data/admin/monthly";
import { createCreditScoreOverviewAdminResponse } from "test/data/admin/overview";
import { createRiskClassification } from "test/data/admin/risk-classification";

import { checkIfAuthenticated, TOKEN_HEADER_NAME } from "../common";
import { adminBase, IS_ADMIN_AUTHENTICATED_KEY } from "./common";

const creditReportHandlers = [
  rest.get(
    `${adminBase}/users/:userId/credit-report`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(ctx.json(createCreditScoreAdminResponse()));
    }
  ),

  rest.get(
    `${adminBase}/users/:userId/credit-report/overview`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(ctx.json(createCreditScoreOverviewAdminResponse()));
    }
  ),

  rest.get(
    `${adminBase}/users/:userId/credit-report/months`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(ctx.json(createCreditScoreMonthlyAdminResponse()));
    }
  ),

  rest.get(
    `${adminBase}/users/:userId/credit-report/categories`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(ctx.json(createCreditScoreCategorisedResponse()));
    }
  ),

  rest.get(
    `${adminBase}/users/:userId/credit-report/risk-classification`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );
      if (!isAuthenticated) {
        return res(ctx.status(401));
      }

      return res(ctx.json(createRiskClassification()));
    }
  ),

  rest.get(
    `${adminBase}/users/:id/credit-report/download`,
    async (req, res, ctx) => {
      const isAuthenticated = !!checkIfAuthenticated(
        IS_ADMIN_AUTHENTICATED_KEY,
        req.headers.get(TOKEN_HEADER_NAME)
      );

      if (!isAuthenticated) {
        return res(ctx.status(401));
      }
      const zip = new JSZip();
      const zipGTestFile = zip.file("Hello.txt", "Hello World\n");

      const zipMockedFile = await zipGTestFile.generateAsync({
        type: "blob",
        compression: "DEFLATE",
        compressionOptions: {
          level: 6,
        },
      });

      return res(
        ctx.set("Content-Length", zipMockedFile.toString()),
        ctx.set("Content-Type", "application/zip"),
        ctx.body(zipMockedFile)
      );
    }
  ),
];

export { creditReportHandlers };
