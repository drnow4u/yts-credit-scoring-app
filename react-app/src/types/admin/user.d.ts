import { DateTime } from "luxon";

import { InvitationStatus } from "helpers/invitation-status";

export type InvitationStatusString = keyof typeof InvitationStatus;

export interface UserDTO {
  userId: string;
  email: string;
  status: InvitationStatusString;
  dateInvited: string;
  dateStatusUpdated: string | null;
  name: string;
  adminEmail: string;
}

export interface UserInvite {
  name: string;
  email: string;
  clientEmailId?: string;
}

export interface User extends Omit<UserDTO, "dateInvited"> {
  dateInvited: DateTime;
}
