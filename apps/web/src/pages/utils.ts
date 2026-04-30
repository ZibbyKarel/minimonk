export const IN_PROGRESS_STATUSES = new Set([
  "CREATED",
  "STOCK_RESERVED",
  "PAYMENT_PENDING",
  "PAYMENT_FAILED",
  "STOCK_RELEASED",
]);

export function money(value: number) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(value);
}

export function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : "Request failed";
}
