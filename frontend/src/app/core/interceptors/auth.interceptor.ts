import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError, switchMap } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';

/**
 * Professional JWT Authentication Interceptor
 * Automatically adds JWT token to requests and handles token refresh
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  // Skip token for auth endpoints
  if (req.url.includes('/auth/login') || 
      req.url.includes('/auth/register') ||
      req.url.includes('/auth/refresh')) {
    return next(req);
  }

  // Clone request and add authorization header
  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Handle 401 Unauthorized - try to refresh token
      if (error.status === 401 && !req.url.includes('/auth/refresh')) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            // Retry original request with new token
            const newToken = authService.getToken();
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newToken}`
              }
            });
            return next(retryReq);
          }),
          catchError(refreshError => {
            // Refresh failed, logout user
            authService.logout();
            return throwError(() => refreshError);
          })
        );
      }

      // Handle 403 Forbidden
      if (error.status === 403) {
        router.navigate(['/unauthorized']);
      }

      return throwError(() => error);
    })
  );
};
