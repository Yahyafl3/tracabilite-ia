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

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/dashboard`;

  getStats(): Observable<DashboardResponse> {
    return this.http.get<DashboardResponse>(this.baseUrl);
  }
}
