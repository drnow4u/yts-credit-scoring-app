import { setupWorker } from "msw";

import {
  adminHandlers,
  managementHandlers,
  publicHandlers,
} from "./api-handlers";

export const worker = setupWorker(
  ...adminHandlers,
  ...publicHandlers,
  ...managementHandlers
);
