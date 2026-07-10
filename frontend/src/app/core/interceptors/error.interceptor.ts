import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';

/**
 * Global Error Interceptor
 * Centralized error handling for HTTP requests
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Une erreur est survenue';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Erreur: ${error.error.message}`;
        console.error('Client Error:', error.error.message);
      } else {
        // Server-side error
        console.error(`Server Error ${error.status}:`, error.error);
        
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
            errorMessage = error.error?.message || 'Données invalides';
            break;
          case 500:
            errorMessage = 'Erreur serveur';
            break;
          case 503:
            errorMessage = 'Service temporairement indisponible';
            break;
          default:
            errorMessage = error.error?.message || `Erreur ${error.status}`;
        }
      }

      // You can integrate a notification service here
      // this.notificationService.error(errorMessage);

      return throwError(() => ({
        message: errorMessage,
        status: error.status,
        originalError: error
      }));
    })
  );
};
