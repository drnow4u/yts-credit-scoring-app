import { PublicCreditScoringAPI } from "api/PublicCreditScoringAPI";
import { useAxiosMutation } from "helpers/axios-query";

import type { Consent } from "types/consent";

export function useSendConsent() {
  async function sendConsent(consent: Consent["consent"]) {
    return await PublicCreditScoringAPI.sendConsent({ consent });
  }

  return useAxiosMutation(sendConsent);
}
