import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type SupportMessageStatus = 'NEW' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';

export interface CreateSupportMessageRequest {
  name: string;
  email: string;
  subject: string;
  message: string;
}

export interface SupportMessage {
  id: string;
  name: string;
  email: string;
  subject: string;
  message: string;
  status: SupportMessageStatus;
  createdAt: string;
  updatedAt: string;
  processedAt: string | null;
  processedById: string | null;
  processedByName: string | null;
}

export interface SupportMessagePage {
  content: SupportMessage[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface SupportMessageResponse {
  message: string;
}

@Injectable({ providedIn: 'root' })
export class SupportService {
  private readonly http = inject(HttpClient);
  private readonly publicUrl = `${environment.apiUrl}/api/support/messages`;
  private readonly adminUrl = `${environment.apiUrl}/api/admin/support/messages`;

  submitMessage(payload: CreateSupportMessageRequest): Observable<SupportMessageResponse> {
    return this.http.post<SupportMessageResponse>(this.publicUrl, payload);
  }

  getMessages(options: {
    status?: SupportMessageStatus | null;
    q?: string;
    page?: number;
    size?: number;
  } = {}): Observable<SupportMessagePage> {
    let params = new HttpParams();
    if (options.status) {
      params = params.set('status', options.status);
    }
    if (options.q?.trim()) {
      params = params.set('q', options.q.trim());
    }
    params = params.set('page', String(options.page ?? 0));
    params = params.set('size', String(options.size ?? 10));
    return this.http.get<SupportMessagePage>(this.adminUrl, { params });
  }

  getMessageById(id: string): Observable<SupportMessage> {
    return this.http.get<SupportMessage>(`${this.adminUrl}/${id}`);
  }

  updateStatus(id: string, status: SupportMessageStatus): Observable<SupportMessage> {
    return this.http.patch<SupportMessage>(`${this.adminUrl}/${id}/status`, { status });
  }
}
