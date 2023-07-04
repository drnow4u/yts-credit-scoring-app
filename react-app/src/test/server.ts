import { rest } from "msw";
import { setupServer } from "msw/node";

import {
  adminHandlers,
  managementHandlers,
  publicHandlers,
} from "./api-handlers";

const server = setupServer(
  ...adminHandlers,
  ...publicHandlers,
  ...managementHandlers
);

export { rest, server };
