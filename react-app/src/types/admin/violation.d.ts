export interface ViolationsResponse {
  violations: Violation[];
}

export interface Violation {
  fieldName: string;
  message: string;
}
