import jp from "jsonpath";
import { CreditScoreResponse } from "types/admin/creditReport";

import { CreditReport as AdminCreditReport } from "../types/admin/creditReport";
import { CreditReport as UserCreditReport } from "../types/credit-report";
import { base64ToUint8Array, stringToBase64 } from "./base64";

export async function verifySignature(
  creditReport: CreditScoreResponse,
  jsonPaths: string[]
): Promise<boolean> {
  const publicKey = {
    kty: "RSA",
    e: "AQAB",
    use: "sig",
    kid: "2a965c71-842b-4f17-91b2-ff752dd46016",
    n: creditReport.publicKey,
  };

  try {
    const cryptoKey = await window.crypto.subtle.importKey(
      "jwk",
      publicKey,
      {
        name: "RSA-PSS",
        hash: { name: "SHA-256" },
      },
      false,
      ["verify"]
    );

    const digest = buildStringForSignatureVerification(
      creditReport.adminReport,
      jsonPaths
    );

    const isValid = await window.crypto.subtle.verify(
      {
        name: "RSA-PSS",
        saltLength: 32,
      },
      cryptoKey,
      base64ToUint8Array(creditReport.signature),
      base64ToUint8Array(stringToBase64(digest))
    );
    return isValid;
  } catch (err) {
    return false;
  }
}

export function buildStringForSignatureVerification(
  creditReport: AdminCreditReport | UserCreditReport,
  fields: string[]
): string {
  let result = "";
  for (const field of fields) {
    const value: string = jp.query(creditReport, field)[0];

    result += value;
    result += ";";
  }
  return result;
}
