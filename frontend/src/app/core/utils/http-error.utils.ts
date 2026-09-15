import { HttpErrorResponse } from '@angular/common/http';

/**
 * The backend's GlobalExceptionHandler always responds with a JSON body
 * shaped like `{ timestamp, status, error, message, errors? }` — so
 * `err.error` is that object, not a string. Pulling `.message` out of it
 * (or falling back to a string body / a generic message) is what actually
 * surfaces the backend's own wording instead of "[object Object]".
 */
export function extractErrorMessage(err: unknown, fallback: string): string {
  if (!(err instanceof HttpErrorResponse)) {
    return fallback;
  }

  if (err.status === 0) {
    return "Couldn't reach the server. Check your connection and try again.";
  }

  if (typeof err.error === 'string' && err.error.trim()) {
    return err.error;
  }

  if (err.error?.message) {
    return err.error.message;
  }

  return fallback;
}
