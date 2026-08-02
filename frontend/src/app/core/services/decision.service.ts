import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  CreditFeaturesRequest,
  DecisionPageResponse,
  DecisionResponse,
  StatutDecisionEnum,
} from '../models/decision.models';

export interface DecisionSearchParams {
  search?: string;
  statut?: StatutDecisionEnum | '';
  domaine?: string;
  riskLevel?: string;
  decisionFinale?: string;
  validateur?: string;
  fromDate?: string;
  toDate?: string;
  page?: number;
  size?: number;
}

@Injectable({ providedIn: 'root' })
export class DecisionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/decisions`;

  analyze(request: CreditFeaturesRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/analyze`, request);
  }

  createCredit(payload: Record<string, unknown>): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/credit`, payload);
  }

  createMedical(payload: Record<string, unknown>): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/medical`, payload);
  }

  createEducation(payload: Record<string, unknown>): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/education`, payload);
  }

  submitForValidation(id: string): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/${id}/submit`, {});
  }

  validateDomain(
    id: string,
    body: {
      decisionFinale: string;
      justificationHumaine?: string;
      accordAvecIa?: boolean;
    },
  ): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/${id}/validate`, body);
  }

  byDomain(domain: string): Observable<DecisionResponse[]> {
    return this.http.get<DecisionResponse[]>(`${this.baseUrl}/domain/${domain}`);
  }

  pendingValidation(): Observable<DecisionResponse[]> {
    return this.http.get<DecisionResponse[]>(`${this.baseUrl}/pending-validation`);
  }

  exportDecisions(params: {
    format: 'csv' | 'xlsx';
    domaine?: string;
    statut?: string;
    validateur?: string;
    fromDate?: string;
    toDate?: string;
  }): Observable<Blob> {
    let httpParams = new HttpParams().set('format', params.format);
    if (params.domaine) httpParams = httpParams.set('domaine', params.domaine);
    if (params.statut) httpParams = httpParams.set('statut', params.statut);
    if (params.validateur) httpParams = httpParams.set('validateur', params.validateur);
    if (params.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
    if (params.toDate) httpParams = httpParams.set('toDate', params.toDate);
    return this.http.get(`${this.baseUrl}/export`, {
      params: httpParams,
      responseType: 'blob',
    });
  }

  retryFailedAgents(id: string): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/${id}/retry-failed-agents`, {});
  }

  getById(id: string): Observable<DecisionResponse> {
    return this.http.get<DecisionResponse>(`${this.baseUrl}/${id}`);
  }

  search(params: DecisionSearchParams): Observable<DecisionPageResponse> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));

    if (params.search?.trim()) httpParams = httpParams.set('search', params.search.trim());
    if (params.statut) httpParams = httpParams.set('statut', params.statut);
    if (params.domaine) httpParams = httpParams.set('domaine', params.domaine);
    if (params.riskLevel) httpParams = httpParams.set('riskLevel', params.riskLevel);
    if (params.decisionFinale) httpParams = httpParams.set('decisionFinale', params.decisionFinale);
    if (params.validateur?.trim()) httpParams = httpParams.set('validateur', params.validateur.trim());
    if (params.fromDate) httpParams = httpParams.set('fromDate', params.fromDate);
    if (params.toDate) httpParams = httpParams.set('toDate', params.toDate);

    return this.http.get<DecisionPageResponse>(this.baseUrl, { params: httpParams });
  }
}
