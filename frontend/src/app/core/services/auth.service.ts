import { Injectable, PLATFORM_ID, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap, finalize } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  LoginCredentials,
  AuthResponse,
  User,
  AuthState,
  PasswordResetRequest,
  PasswordReset,
  TokenPayload
} from '../models/auth.models';

/**
 * Professional Authentication Service
 * Handles all authentication operations with best practices
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly isBrowser = isPlatformBrowser(inject(PLATFORM_ID));
  
  private readonly API_URL = `${environment.apiUrl}/api/auth`;
  private readonly TOKEN_KEY = 'auth_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'user_data';

  // Reactive state management
  private authState = signal<AuthState>({
    user: null,
    token: null,
    isAuthenticated: false,
    isLoading: false,
    error: null
  });

  // BehaviorSubject for backward compatibility
  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor() {
    this.initializeAuthState();
  }

  /**
   * Initialize authentication state from storage
   */
  private initializeAuthState(): void {
    const token = this.getToken();
    const user = this.getUserFromStorage();

    if (token && user && !this.isTokenExpired(token)) {
      this.authState.update(state => ({
        ...state,
        user,
        token,
        isAuthenticated: true
      }));
      this.currentUserSubject.next(user);
    } else {
      this.clearAuthData();
    }
  }

  /**
   * User login
   */
  login(credentials: LoginCredentials): Observable<AuthResponse> {
    this.setLoading(true);

    return this.http.post<AuthResponse>(`${this.API_URL}/login`, credentials).pipe(
      tap(response => this.handleAuthSuccess(response, credentials.rememberMe)),
      catchError(error => this.handleAuthError(error)),
      finalize(() => this.setLoading(false))
    );
  }

  /**
   * User logout
   */
  logout(): void {
    const token = this.getToken();
    
    if (token) {
      // Notify backend (fire and forget)
      this.http.post(`${this.API_URL}/logout`, {}).subscribe({
        error: () => console.warn('Logout notification failed')
      });
    }

    this.clearAuthData();
    this.router.navigate(['/auth/login']);
  }

  /**
   * Refresh access token
   */
  refreshToken(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();

    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.http.post<AuthResponse>(`${this.API_URL}/refresh`, { refreshToken }).pipe(
      tap(response => {
        this.setToken(response.token);
        if (response.refreshToken) {
          this.setRefreshToken(response.refreshToken);
        }
      }),
      catchError(error => {
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Request password reset
   */
  requestPasswordReset(request: PasswordResetRequest): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/password-reset/request`, request);
  }

  /**
   * Reset password
   */
  resetPassword(reset: PasswordReset): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/password-reset/confirm`, reset);
  }

  /**
   * Verify email
   */
  verifyEmail(token: string): Observable<void> {
    return this.http.post<void>(`${this.API_URL}/verify-email`, { token });
  }

  /**
   * Get current user profile
   */
  getCurrentUserProfile(): Observable<User> {
    return this.http.get<User>(`${this.API_URL}/profile`).pipe(
      tap(user => {
        this.setUser(user);
        this.currentUserSubject.next(user);
      })
    );
  }

  /**
   * Update user profile
   */
  updateProfile(updates: Partial<User>): Observable<User> {
    return this.http.put<User>(`${this.API_URL}/profile`, updates).pipe(
      tap(user => {
        this.setUser(user);
        this.currentUserSubject.next(user);
      })
    );
  }

  // ==================== Getters ====================

  get currentUser(): User | null {
    return this.authState().user;
  }

  get isAuthenticated(): boolean {
    return this.authState().isAuthenticated;
  }

  get isLoading(): boolean {
    return this.authState().isLoading;
  }

  getToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.TOKEN_KEY) || sessionStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    if (!this.isBrowser) {
      return null;
    }
    return localStorage.getItem(this.REFRESH_TOKEN_KEY) || sessionStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  // ==================== Helpers ====================

  private handleAuthSuccess(response: AuthResponse, rememberMe: boolean = false): void {
    const storage = rememberMe ? localStorage : sessionStorage;

    storage.setItem(this.TOKEN_KEY, response.token);
    if (response.refreshToken) {
      storage.setItem(this.REFRESH_TOKEN_KEY, response.refreshToken);
    }
    storage.setItem(this.USER_KEY, JSON.stringify(response.user));

    this.authState.update(state => ({
      ...state,
      user: response.user,
      token: response.token,
      isAuthenticated: true,
      error: null
    }));

    this.currentUserSubject.next(response.user);
  }

  private handleAuthError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur est survenue';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = `Erreur: ${error.error.message}`;
    } else {
      // Server-side error
      switch (error.status) {
        case 401:
          errorMessage = 'Email ou mot de passe incorrect';
          break;
        case 403:
          errorMessage = 'Accès refusé';
          break;
        case 404:
          errorMessage = 'Service non disponible';
          break;
        case 422:
          errorMessage = error.error?.message || 'Données invalides';
          break;
        case 500:
          errorMessage = 'Erreur serveur. Veuillez réessayer plus tard';
          break;
        default:
          errorMessage = error.error?.message || errorMessage;
      }
    }

    this.authState.update(state => ({ ...state, error: errorMessage }));
    return throwError(() => new Error(errorMessage));
  }

  private setLoading(loading: boolean): void {
    this.authState.update(state => ({ ...state, isLoading: loading }));
  }

  private setToken(token: string): void {
    const storage = localStorage.getItem(this.TOKEN_KEY) ? localStorage : sessionStorage;
    storage.setItem(this.TOKEN_KEY, token);
  }

  private setRefreshToken(token: string): void {
    const storage = localStorage.getItem(this.REFRESH_TOKEN_KEY) ? localStorage : sessionStorage;
    storage.setItem(this.REFRESH_TOKEN_KEY, token);
  }

  private setUser(user: User): void {
    const storage = localStorage.getItem(this.USER_KEY) ? localStorage : sessionStorage;
    storage.setItem(this.USER_KEY, JSON.stringify(user));
    this.authState.update(state => ({ ...state, user }));
  }

  private getUserFromStorage(): User | null {
    if (!this.isBrowser) {
      return null;
    }
    const userData = localStorage.getItem(this.USER_KEY) || sessionStorage.getItem(this.USER_KEY);
    if (userData) {
      try {
        return JSON.parse(userData);
      } catch {
        return null;
      }
    }
    return null;
  }

  private clearAuthData(): void {
    if (this.isBrowser) {
      [localStorage, sessionStorage].forEach(storage => {
        storage.removeItem(this.TOKEN_KEY);
        storage.removeItem(this.REFRESH_TOKEN_KEY);
        storage.removeItem(this.USER_KEY);
      });
    }

    this.authState.update(state => ({
      ...state,
      user: null,
      token: null,
      isAuthenticated: false,
      error: null
    }));

    this.currentUserSubject.next(null);
  }

  private isTokenExpired(token: string): boolean {
    try {
      const payload = this.decodeToken(token);
      const now = Date.now() / 1000;
      return payload.exp < now;
    } catch {
      return true;
    }
  }

  private decodeToken(token: string): TokenPayload {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    );
    return JSON.parse(jsonPayload);
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    return this.currentUser?.role === role;
  }

  /**
   * Check if user has any of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.hasRole(role));
  }
}
