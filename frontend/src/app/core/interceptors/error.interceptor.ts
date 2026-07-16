import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

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

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const fromBackend = backendMessage(error);
      let errorMessage = fromBackend ?? 'Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        errorMessage = `Erreur: ${error.error.message}`;
        console.error('Client Error:', error.error.message);
      } else {
        console.error(`Server Error ${error.status}:`, error.error);

        if (!fromBackend) {
          switch (error.status) {
            case 400:
              errorMessage = 'Requête invalide';
              break;
            case 401:
              errorMessage = 'Non authentifié';
              break;
            case 403:
              errorMessage = 'Accès refusé';
              break;
            case 404:
              errorMessage = 'Ressource non trouvée';
              break;
            case 409:
              errorMessage = 'Conflit de données';
              break;
            case 422:
              errorMessage = 'Données invalides';
              break;
            case 500:
              errorMessage = 'Erreur serveur';
              break;
            case 503:
              errorMessage = 'Service temporairement indisponible';
              break;
            default:
              errorMessage = `Erreur ${error.status}`;
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
