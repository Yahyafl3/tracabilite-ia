import { HttpErrorResponse } from '@angular/common/http';

/**
 * Extrait le message d'erreur renvoyé par le backend ou l'intercepteur HTTP.
 */
export function resolveHttpErrorMessage(
  err: unknown,
  fallback = 'Une erreur est survenue',
): string {
  if (!err || typeof err !== 'object') {
    return fallback;
  }

  const wrapped = err as {
    message?: string;
    error?: { message?: string; error?: string };
    originalError?: HttpErrorResponse;
  };

  if (wrapped.message && wrapped.message !== 'Une erreur est survenue') {
    return wrapped.message;
  }

  const body = wrapped.error ?? wrapped.originalError?.error;
  if (typeof body === 'string' && body.trim()) {
    return body;
  }
  if (body && typeof body === 'object') {
    const record = body as { message?: string; error?: string };
    if (record.message) {
      return record.message;
    }
    if (record.error) {
      return record.error;
    }
  }

  return fallback;
}
