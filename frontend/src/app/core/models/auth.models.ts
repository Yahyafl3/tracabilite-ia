/**
 * Authentication Models
 * Professional-grade type definitions for authentication system
 */

export interface LoginCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterData {
  email: string;
  password: string;
  confirmPassword: string;
  nom: string;
  prenom: string;
  role: UserRole;
  acceptTerms: boolean;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface User {
  id: number;
  email: string;
  nom: string;
  prenom: string;
  role: UserRole;
  actif: boolean;
  avatar?: string;
  dateCreation: Date;
  derniereConnexion?: Date;
}

export enum UserRole {
  ADMINISTRATEUR = 'ADMINISTRATEUR',
  VALIDATEUR = 'VALIDATEUR',
  UTILISATEUR = 'UTILISATEUR'
}

export interface TokenPayload {
  sub: string;
  email: string;
  role: UserRole;
  iat: number;
  exp: number;
}

export interface PasswordResetRequest {
  email: string;
}

export interface PasswordReset {
  token: string;
  newPassword: string;
  confirmPassword: string;
}

export interface AuthState {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
}
