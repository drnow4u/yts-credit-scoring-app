import axios from "axios";

import {
  ADMIN_TOKEN_STORAGE_NAME,
  HOMEPAGE,
  TOKEN_HEADER_NAME,
  USER_TOKEN_STORAGE_NAME,
} from "helpers/constants";

const storedUserToken = localStorage.getItem(USER_TOKEN_STORAGE_NAME);
const userToken = storedUserToken?.startsWith("Bearer ")
  ? storedUserToken
  : undefined;

export const PublicHTTPClient = axios.create({
  baseURL: `${HOMEPAGE}api/user`,
  headers: {
    ...(!!userToken && { [TOKEN_HEADER_NAME]: userToken }),
  },
});

const storedAdminToken = sessionStorage.getItem(ADMIN_TOKEN_STORAGE_NAME);
const adminToken = storedAdminToken?.startsWith("Bearer ")
  ? storedAdminToken
  : undefined;

export const AdminHTTPClient = axios.create({
  baseURL: `${HOMEPAGE}api/admin`,
  headers: {
    ...(!!adminToken && { [TOKEN_HEADER_NAME]: adminToken }),
  },
});

//FIXME: temporary implementation, have to be reviewed when backend will be ready
export const ManagementHTTPClient = axios.create({
  baseURL: `${HOMEPAGE}api/management`,
  headers: {
    ...(!!adminToken && { [TOKEN_HEADER_NAME]: adminToken }),
  },
});
