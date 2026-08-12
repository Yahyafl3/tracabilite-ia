import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import type { ComparaisonAgent } from './comparaison.service';

// Legacy interfaces for existing dashboard functionality
export interface DashboardRecentDecision {
  decisionId: string;
  prompt: string;
  modelName: string;
  agentLabel: string;
  statutValidation: 'APPROUVEE' | 'MODIFIEE' | 'REJETEE' | 'EN_ATTENTE' | 'BROUILLON';
  timestamp: string;
}

export interface DashboardResponse {
  totalDecisions: number;
  approuvees: number;
  modifiees: number;
  rejetees: number;
  enAttente: number;
  brouillon: number;
  tauxValidation: number;
  agentsActifs: number;
  agentsLabel: string;
  hashChainIntact: boolean;
  generatedAt: string;
  recentDecisions: DashboardRecentDecision[];
  agentPerformance: ComparaisonAgent[];
}

// Professional Role Dashboards DTOs
export interface KPIValues {
  totalDecisions: number;
  pendingValidations: number;
  todaysDecisions: number;
  activeUsers: number;
  validatedDecisions: number;
  rejectedDecisions: number;
  acceptanceRate: number;
  validationRate: number;
  processedDecisions: number;
  complianceRate: number;
}

export interface TimelineDataPoint {
  date: string; // LocalDate from backend
  decisionCount: number;
  validationCount: number;
}

export interface StatusDistribution {
  enAttenteValidation: number;
  validee: number;
  rejetee: number;
}

export interface DomainDistribution {
  creditCount: number;
  medicalCount: number;
  educationCount: number;
}

export interface CreatorStats {
  creatorEmail: string;
  creatorName: string;
  decisionCount: number;
}

export interface RecentDecisionDTO {
  decisionId: string;
  reference: string;
  domaine: 'CREDIT' | 'MEDICAL' | 'EDUCATION';
  statutValidation: 'EN_ATTENTE_VALIDATION' | 'VALIDEE' | 'REJETEE';
  createdAt: string; // LocalDateTime from backend
  createdBy: string;
  creatorName?: string;
}

export interface ValidationActionDTO {
  decisionId: string;
  decisionReference: string;
  domaine: 'CREDIT' | 'MEDICAL' | 'EDUCATION';
  statutValidation: 'EN_ATTENTE_VALIDATION' | 'VALIDEE' | 'REJETEE';
  validatedAt: string; // LocalDateTime from backend
  validatedBy: string;
  validatorName?: string;
  commentaire?: string;
}

export interface DashboardStatsDTO {
  kpiValues: KPIValues;
  timelineData: TimelineDataPoint[];
  statusDistribution: StatusDistribution;
  domainDistribution?: DomainDistribution;
  topCreators?: CreatorStats[];
  recentDecisions: RecentDecisionDTO[];
  recentValidations?: ValidationActionDTO[];
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/dashboard`;

  /**
   * Legacy method for existing dashboard functionality
   * @deprecated Use getDashboardStats() for new professional role dashboards
   * This method is disabled to prevent calls to the non-existent /api/dashboard endpoint
   */
  getStats(): Observable<DashboardResponse> {
    throw new Error('getStats() is deprecated. Use getDashboardStats() instead. The /api/dashboard endpoint does not exist.');
  }

  /**
   * Get dashboard statistics for the authenticated user's role
   * Implements requirements 1.1, 9.3, and 13.7
   * @returns Observable of DashboardStatsDTO with role-scoped statistics
   */
  getDashboardStats(): Observable<DashboardStatsDTO> {
    return this.http.get<DashboardStatsDTO>(`${this.baseUrl}/stats`).pipe(
      timeout(10000), // 10 second timeout per requirement 13.7
      catchError(this.handleError)
    );
  }

  /**
   * Handle HTTP errors with user-friendly messages
   * Implements requirement 9.3 (error handling)
   * @param error HttpErrorResponse from the HTTP call
   * @returns Observable that errors with a descriptive message
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let errorMessage = 'Une erreur est survenue';

    if (error.status === 0) {
      // Network error - client couldn't reach server
      errorMessage = 'Impossible de contacter le serveur. Vérifiez votre connexion.';
    } else if (error.status === 403) {
      // Forbidden - user doesn't have access
      errorMessage = 'Accès refusé. Vous n\'avez pas les permissions nécessaires.';
    } else if (error.status === 500) {
      // Server error
      errorMessage = 'Erreur serveur. Veuillez réessayer ultérieurement.';
    } else if (error.error?.message) {
      // Use backend error message if available
      errorMessage = error.error.message;
    }

    console.error('Dashboard service error:', error);
    return throwError(() => new Error(errorMessage));
  }
}
