import axios, { AxiosError } from "axios";

export function isAxiosError<T = any>(
  err: AxiosError | Error | unknown
): err is AxiosError<T> {
  if (err === null) return false;

  return axios.isAxiosError(err);
}
