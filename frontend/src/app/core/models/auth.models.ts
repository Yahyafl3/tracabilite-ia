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

export interface JwtResponse {
  token: string;
  type: string;
  id: string;
  nom: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  refreshToken?: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

export interface User {
  id: string;
  email: string;
  nom: string;
  prenom?: string;
  role: UserRole | string;
  actif?: boolean;
  avatar?: string;
  dateCreation?: Date;
  derniereConnexion?: Date;
}

/**
 * Rôles des comptes internes. UTILISATEUR/VALIDATEUR ont été migrés vers AGENT_CREDIT et
 * RESPONSABLE_CREDIT ; ils ne donnent plus accès à aucune route.
 */
export enum UserRole {
  ADMINISTRATEUR = 'ADMINISTRATEUR',
  AUDITEUR = 'AUDITEUR',
  /** Agent créateur — décisions CREDIT, lecture propres dossiers. */
  AGENT_CREDIT = 'AGENT_CREDIT',
  /** Agent créateur — décisions MEDICAL, lecture propres dossiers. */
  AGENT_SANTE = 'AGENT_SANTE',
  /** Agent créateur — décisions EDUCATION, lecture propres dossiers. */
  AGENT_PEDAGOGIQUE = 'AGENT_PEDAGOGIQUE',
  RESPONSABLE_CREDIT = 'RESPONSABLE_CREDIT',
  PROFESSIONNEL_SANTE = 'PROFESSIONNEL_SANTE',
  RESPONSABLE_PEDAGOGIQUE = 'RESPONSABLE_PEDAGOGIQUE',
  /** @deprecated Migré vers AGENT_CREDIT. Conservé pour lire les JWT émis avant la migration. */
  UTILISATEUR = 'UTILISATEUR',
  /** @deprecated Migré vers RESPONSABLE_CREDIT. Conservé pour lire les JWT émis avant la migration. */
  VALIDATEUR = 'VALIDATEUR',
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
