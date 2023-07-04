import packageInfo from "../../package.json";

export const TOKEN_HEADER_NAME = "Authorization";

export const ADMIN_TOKEN_STORAGE_NAME = "admin_token";
export const USER_HASH_STORAGE_NAME = "user_hash";
export const USER_TOKEN_STORAGE_NAME = "user_token";
export const USER_LANGUAGE_NAME = "user_language";

export const POLLING_STATUS = 202;

export const USERS_TABLE_POLLING_INTERVAL =
  process.env.REACT_APP_USERS_TABLE_POLLING_INTERVAL !== undefined
    ? Number.parseInt(process.env.REACT_APP_USERS_TABLE_POLLING_INTERVAL)
    : NaN;

export const USERS_TABLE_POLLING = !!USERS_TABLE_POLLING_INTERVAL;

export const HOMEPAGE = packageInfo.homepage;

export const THROTTLE_API_CALL_TIMEOUT = 300;
