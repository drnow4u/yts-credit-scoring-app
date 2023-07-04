import { readSessionStorage } from "test/utils/storage";

export const TOKEN_HEADER_NAME = "Authorization";

export function checkIfAuthenticated(key: string, token: string | null) {
  try {
    if (token?.startsWith("Bearer ")) {
      return readSessionStorage<boolean>(key);
    }
  } catch (error) {
    return false;
  }
}
