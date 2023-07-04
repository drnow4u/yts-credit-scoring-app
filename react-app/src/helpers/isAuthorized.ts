import { ADMIN_TOKEN_STORAGE_NAME } from "helpers/constants";

export const isAuthorized = () => {
  const isTokenStored = sessionStorage.getItem(ADMIN_TOKEN_STORAGE_NAME);

  if (process.env.NODE_ENV !== "production") {
    const isAdminAuthenticated = sessionStorage.getItem("isAdminAuthenticated");

    if (isAdminAuthenticated || isTokenStored) return true;
  }

  return !!isTokenStored;
};

export default isAuthorized;
