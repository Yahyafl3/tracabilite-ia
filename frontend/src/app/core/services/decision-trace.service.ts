import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type DecisionHistoryAction =
  | 'DECISION_CREATED'
  | 'ML_ANALYSIS_STARTED'
  | 'ML_ANALYSIS_COMPLETED'
  | 'ML_ANALYSIS_FAILED'
  | 'OPENROUTER_ANALYSIS_STARTED'
  | 'AGENT_RESPONSE_SUCCESS'
  | 'AGENT_RESPONSE_FAILED'
  | 'CONSENSUS_CALCULATED'
  | 'DECISION_SUBMITTED_FOR_VALIDATION'
  | 'DECISION_APPROVED'
  | 'DECISION_REJECTED'
  | 'DECISION_MODIFIED'
  | 'SOURCE_ADDED'
  | 'SOURCE_REMOVED'
  | 'INTEGRITY_VERIFIED'
  | 'DECISION_ARCHIVED';

export interface DecisionHistoryEntry {
  historyId: string;
  decisionId: string;
  action: DecisionHistoryAction;
  previousStatus?: string;
  newStatus?: string;
  performedById?: string;
  performedByEmail?: string;
  comment?: string;
  justification?: string;
  eventData?: Record<string, unknown>;
  correlationId?: string;
  createdAt: string;
}

export type DecisionSourceType =
  | 'USER_INPUT'
  | 'BUSINESS_DATA'
  | 'DOCUMENT'
  | 'URL'
  | 'DATABASE'
  | 'MODEL_OUTPUT'
  | 'OTHER';

export interface DecisionSource {
  sourceId: string;
  decisionId: string;
  sourceType: DecisionSourceType;
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

export interface CreateDecisionSourceRequest {
  sourceType: DecisionSourceType;
  name: string;
  description?: string;
  url?: string;
  documentReference?: string;
  metadata?: Record<string, unknown>;
}

@Injectable({ providedIn: 'root' })
export class DecisionTraceService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/decisions`;

  getHistory(decisionId: string): Observable<DecisionHistoryEntry[]> {
    return this.http.get<DecisionHistoryEntry[]>(`${this.baseUrl}/${decisionId}/history`);
  }

  getSources(decisionId: string): Observable<DecisionSource[]> {
    return this.http.get<DecisionSource[]>(`${this.baseUrl}/${decisionId}/sources`);
  }

  addSource(decisionId: string, request: CreateDecisionSourceRequest): Observable<DecisionSource> {
    return this.http.post<DecisionSource>(`${this.baseUrl}/${decisionId}/sources`, request);
  }

  removeSource(decisionId: string, sourceId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${decisionId}/sources/${sourceId}`);
  }
}
