import { useAxiosMutation } from "helpers/axios-query";

import { InvalidSignature } from "../../types/admin/invalid-signature";
import { log } from "./creditScoringApi";

export const QUERY_KEY = "backend-logger";

export const useBackendLogger = () => {
  async function sendLog(invalidSignature: InvalidSignature) {
    return log(invalidSignature);
  }

  return useAxiosMutation(sendLog);
};
