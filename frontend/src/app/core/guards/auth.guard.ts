import { inject } from '@angular/core';
import { Router, CanActivateFn, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Authentication Guard
 * Protects routes that require authentication
 */
export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated) {
    return true;
  }

  // Store the attempted URL for redirecting after login
  const returnUrl = state.url;
  router.navigate(['/auth/login'], { 
    queryParams: { returnUrl },
    replaceUrl: true 
  });
  
  return false;
};

/**
 * Role Guard Factory
 * Creates a guard that checks for specific roles
 */
export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return (route: ActivatedRouteSnapshot, state: RouterStateSnapshot) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated) {
      router.navigate(['/auth/login'], { 
        queryParams: { returnUrl: state.url },
        replaceUrl: true 
      });
      return false;
    }

    if (authService.hasAnyRole(allowedRoles)) {
      return true;
    }

    // User doesn't have required role
    router.navigate(['/unauthorized']);
    return false;
  };
};

/**
 * Guest Guard
 * Prevents authenticated users from accessing auth pages
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated) {
    return true;
  }

  router.navigate(['/decisions']);
  return false;
};
