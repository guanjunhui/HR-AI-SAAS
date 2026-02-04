export interface PageResponse<T> {
  pageNo: number;
  pageSize: number;
  total: number;
  records: T[];
}
