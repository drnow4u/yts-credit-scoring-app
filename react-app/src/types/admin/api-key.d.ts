import { InvitationStatus } from "helpers/invitation-status";

export type InvitationStatusString = keyof typeof InvitationStatus;

export interface CreateApiKeyResponse {
  clientToken: string;
}

export interface ApiKeyDetails {
  name: string;
  permissions: string[];
}

export type ApiPermissionsResponse = string[];

export type ApiTokensDetails = {
  id: string;
  name: string;
  expiryDate: string;
  creationDate: string;
  lastUsed?: string | null;
  status: "ACTIVE" | "REVOKE";
};

export type GetApiTokensDetailsResponse = ApiTokensDetails[];
