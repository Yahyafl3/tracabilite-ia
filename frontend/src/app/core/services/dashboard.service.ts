import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { ComparaisonAgent } from './comparaison.service';

export interface DashboardRecentDecision {
  decisionId: string;
  prompt: string;
  modelName: string;
  agentLabel: string;
  statutValidation: 'APPROUVEE' | 'MODIFIEE' | 'REJETEE' | 'EN_ATTENTE' | 'BROUILLON';
  timestamp: string;
  riskLevel?: string;
  confidenceScore?: number;
  reference?: string;
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

export interface TimelineData {
  label: string;
  created: number;
  solved: number;
}

export interface TypeStats {
  counts: Record<string, number>;
}

export interface DailyStats {
  counts: Record<string, number>;
}

export interface KpiData {
  approvalRate: number;
  highRiskCount: number;
  newTickets: number;
  returnedTickets: number;
  domainMetrics?: Record<string, any>;
  riskBreakdown?: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/dashboard`;

  getStats(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(this.baseUrl);
  }

  getTimelineStats(): Observable<TimelineData[]> {
    return this.http.get<TimelineData[]>(`${this.baseUrl}/stats/timeline`);
  }

  getTypeStats(): Observable<TypeStats> {
    return this.http.get<TypeStats>(`${this.baseUrl}/stats/by-type`);
  }

  getDailyStats(): Observable<DailyStats> {
    return this.http.get<DailyStats>(`${this.baseUrl}/stats/daily`);
  }

  getKpiStats(): Observable<KpiData> {
    return this.http.get<KpiData>(`${this.baseUrl}/stats/kpi`);
  }
}
