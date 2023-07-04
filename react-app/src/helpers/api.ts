import { POLLING_STATUS } from "./constants";

export function checkPollingStatus(status: number) {
  if (status === POLLING_STATUS) {
    return false;
  }
  return status >= 200 && status < 300; // default
}

export interface PaginationData {
  pageNumber: number;
  totalPages: number;
  pageSize: number;
  totalItemCount: number;
}

export function extractPaginationData(headers: any): PaginationData {
  const pageNumber = parseInt(headers["x-pagination-page"] ?? "", 10);
  const totalPages = parseInt(headers["x-pagination-pages"] ?? "", 10);
  const pageSize = parseInt(headers["x-pagination-pagesize"] ?? "", 10);
  const totalItemCount = parseInt(headers["x-total-count"] ?? "", 10);
  return { pageNumber, totalPages, pageSize, totalItemCount };
}
