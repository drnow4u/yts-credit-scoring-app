export interface Site {
  id: string;
  name: string;
}

export interface ConnectSiteResponse {
  redirectUrl: string;
}

export interface CreateUserSiteResponse {
  activityId: string | null;
  redirectUrl: string | null;
}
