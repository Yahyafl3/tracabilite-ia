import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { TranslationService } from '../i18n/translation.service';

function backendMessage(error: HttpErrorResponse): string | null {
  const body = error.error;
  if (typeof body === 'string' && body.trim()) {
    return body;
  }
  if (body && typeof body === 'object') {
    if (typeof body.message === 'string' && body.message.trim()) {
      return body.message;
    }
    if (typeof body.error === 'string' && body.error.trim()) {
      return body.error;
    }
  }
  return null;
}

/**
 * Global Error Interceptor
 * Centralized error handling for HTTP requests
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const i18n = inject(TranslationService, { optional: true });
  const t = (key: string, fallback: string, params?: Record<string, unknown>) =>
    i18n ? i18n.t(key, params) : fallback;

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const fromBackend = backendMessage(error);
      let errorMessage = fromBackend ?? t('httpErrors.generic', 'Une erreur est survenue');

      if (error.error instanceof ErrorEvent) {
        errorMessage = `${t('httpErrors.generic', 'Erreur')}: ${error.error.message}`;
        console.error('Client Error:', error.error.message);
      } else {
        console.error(`Server Error ${error.status}:`, error.error);

        if (!fromBackend) {
          switch (error.status) {
            case 400:
              errorMessage = t('httpErrors.invalidRequest', 'Requête invalide');
              break;
            case 401:
              errorMessage = t('httpErrors.unauthenticated', 'Non authentifié');
              break;
            case 403:
              errorMessage = t('httpErrors.forbidden', 'Accès refusé');
              break;
            case 404:
              errorMessage = t('httpErrors.notFound', 'Ressource non trouvée');
              break;
            case 409:
              errorMessage = t('httpErrors.conflict', 'Conflit de données');
              break;
            case 422:
              errorMessage = t('httpErrors.invalidData', 'Données invalides');
              break;
            case 500:
              errorMessage = t('httpErrors.server', 'Erreur serveur');
              break;
            case 503:
              errorMessage = t('httpErrors.unavailable', 'Service temporairement indisponible');
              break;
            default:
              errorMessage = t('httpErrors.status', `Erreur ${error.status}`, { status: error.status });
          }
        }
      }

      return throwError(() => ({
        message: errorMessage,
        status: error.status,
        originalError: error,
      }));
    })
  );
};
