import { Token } from "types/admin/token";

export function getToken(): Token {
  return {
    access_token: "Bearer fake_access_token",
    token_type: "bearer",
    expires_in: 3600,
  };
}
