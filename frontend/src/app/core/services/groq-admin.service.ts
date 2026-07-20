import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface GroqModelStatus {
  agent: string;
  displayName?: string;
  modelId: string;
  available: boolean;
}

export interface GroqAdminStatus {
  configured: boolean;
  reachable: boolean;
  lastError?: string | null;
  models: GroqModelStatus[];
  successfulResponses?: number;
}

@Injectable({ providedIn: 'root' })
export class GroqAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/admin/groq`;

  getStatus(): Observable<GroqAdminStatus> {
    return this.http.get<GroqAdminStatus>(`${this.baseUrl}/status`);
  }
}
