import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DecisionPageResponse, DecisionResponse } from '../models/decision.models';
import { ValidationActionResponse, ValidationRequest } from '../models/validation.models';

@Injectable({ providedIn: 'root' })
export class ValidationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api`;
  private readonly validationUrl = `${this.baseUrl}/validation`;
  private readonly decisionsUrl = `${this.baseUrl}/decisions`;

  getPending(page = 0, size = 10): Observable<DecisionPageResponse> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size));
    return this.http.get<DecisionPageResponse>(`${this.validationUrl}/pending`, { params });
  }

  getHistory(decisionId: string): Observable<ValidationActionResponse[]> {
    return this.http.get<ValidationActionResponse[]>(
      `${this.validationUrl}/decision/${decisionId}/history`,
    );
  }

  approve(decisionId: string, request?: ValidationRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(
      `${this.decisionsUrl}/${decisionId}/approve`,
      request ?? {},
    );
  }

  reject(decisionId: string, request?: ValidationRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(
      `${this.decisionsUrl}/${decisionId}/reject`,
      request ?? {},
    );
  }

  modify(decisionId: string, request: ValidationRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(
      `${this.decisionsUrl}/${decisionId}/modify`,
      request,
    );
  }
}
