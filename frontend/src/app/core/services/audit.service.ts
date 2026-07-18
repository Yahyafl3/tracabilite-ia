import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { StatutDecisionEnum } from '../models/decision.models';
import type { AgentResponse, ConsensusResponse } from '../models/openrouter.models';
import type { ValidationActionResponse } from '../models/validation.models';

export interface AuditRecentItemResponse {
  decisionId: string;
  prompt: string;
  statutValidation: StatutDecisionEnum;
  integrityValid: boolean;
  timestamp: string;
}

export interface AuditRecentResponse {
  items: AuditRecentItemResponse[];
  generatedAt: string;
}

export interface AuditIntegritySummaryResponse {
  totalDecisions: number;
  validDecisions: number;
  invalidDecisions: number;
  chainIntact: boolean;
  generatedAt: string;
}

export interface AuditDecisionHistoryResponse {
  historyId: string;
  decisionId: string;
  action: string;
  previousStatus?: StatutDecisionEnum;
  newStatus?: StatutDecisionEnum;
  performedById?: string;
  performedByEmail?: string;
  comment?: string;
  justification?: string;
  eventData?: Record<string, unknown>;
  correlationId?: string;
  createdAt: string;
}

export interface AuditDecisionSourceResponse {
  sourceId: string;
  decisionId: string;
  sourceType: string;
  name: string;
  description?: string;
  url?: string;
  documentReference?: string;
  contentHash: string;
  metadata?: Record<string, unknown>;
  createdById?: string;
  createdByEmail?: string;
  createdAt: string;
}

export interface AuditDecisionResponse {
  decisionId: string;
  prompt: string;
  contexte: string;
  modelName: string;
  modelVersion?: string;
  suggestedDecision?: string;
  confidenceScore?: number;
  riskLevel?: string;
  explanationSource?: string;
  reponse: string;
  consensus?: ConsensusResponse;
  resumeConsensus?: string;
  statutValidation: StatutDecisionEnum;
  humanDecision?: string;
  validatorEmail?: string;
  businessDataHash?: string;
  sourcesHash?: string;
  agentResponsesHash?: string;
  previousHash?: string;
  currentHash?: string;
  integrityValid: boolean;
  timestamp: string;
  agentResponses?: AgentResponse[];
  validations?: ValidationActionResponse[];
  history?: AuditDecisionHistoryResponse[];
  sources?: AuditDecisionSourceResponse[];
}

@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/audit`;

  getRecent(limit = 20): Observable<AuditRecentResponse> {
    const params = new HttpParams().set('limit', String(limit));
    return this.http.get<AuditRecentResponse>(`${this.baseUrl}/recent`, { params });
  }

  getIntegritySummary(): Observable<AuditIntegritySummaryResponse> {
    return this.http.get<AuditIntegritySummaryResponse>(`${this.baseUrl}/integrity/summary`);
  }

  getDecisionAudit(decisionId: string): Observable<AuditDecisionResponse> {
    return this.http.get<AuditDecisionResponse>(`${this.baseUrl}/decisions/${decisionId}`);
  }
}
