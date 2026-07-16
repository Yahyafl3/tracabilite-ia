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

@Injectable({ providedIn: 'root' })
export class DecisionService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/decisions`;

  analyze(request: CreditFeaturesRequest): Observable<DecisionResponse> {
    return this.http.post<DecisionResponse>(`${this.baseUrl}/analyze`, request);
  }

  getById(id: string): Observable<DecisionResponse> {
    return this.http.get<DecisionResponse>(`${this.baseUrl}/${id}`);
  }

  search(params: {
    search?: string;
    statut?: StatutDecisionEnum | '';
    page?: number;
    size?: number;
  }): Observable<DecisionPageResponse> {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));

    if (params.search?.trim()) {
      httpParams = httpParams.set('search', params.search.trim());
    }
    if (params.statut) {
      httpParams = httpParams.set('statut', params.statut);
    }

    return this.http.get<DecisionPageResponse>(this.baseUrl, { params: httpParams });
  }
}
