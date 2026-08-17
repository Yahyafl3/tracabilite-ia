import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type BackupJobStatus =
  | 'CREATED'
  | 'VERIFIED_OK'
  | 'VERIFIED_TAMPERED'
  | 'MISSING_FILE'
  | 'RESTORED';

export interface BackupJob {
  id: string;
  createdAt: string;
  createdByEmail?: string;
  filename: string;
  sizeBytes: number;
  packSha256: string;
  decisionCount: number;
  userCount: number;
  status: BackupJobStatus;
  lastVerifiedAt?: string | null;
  lastRestoredAt?: string | null;
  restoreUsersCreated?: number | null;
  restoreUsersSkipped?: number | null;
  restoreDecisionsCreated?: number | null;
  restoreDecisionsSkipped?: number | null;
  filePresent: boolean;
}

export interface BackupVerifyResult {
  id: string;
  valid: boolean;
  expectedSha256: string;
  actualSha256?: string | null;
  status: BackupJobStatus;
}

export interface BackupRestoreResult {
  id: string;
  usersCreated: number;
  usersSkipped: number;
  decisionsCreated: number;
  decisionsSkipped: number;
  packSha256: string;
}

@Injectable({ providedIn: 'root' })
export class BackupAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/admin/backup`;

  list(): Observable<BackupJob[]> {
    return this.http.get<BackupJob[]>(this.baseUrl);
  }

  create(): Observable<BackupJob> {
    return this.http.post<BackupJob>(this.baseUrl, {});
  }

  verify(id: string): Observable<BackupVerifyResult> {
    return this.http.post<BackupVerifyResult>(`${this.baseUrl}/${id}/verify`, {});
  }

  restore(id: string): Observable<BackupRestoreResult> {
    return this.http.post<BackupRestoreResult>(`${this.baseUrl}/${id}/restore`, { confirm: true });
  }

  download(id: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/${id}/file`, { responseType: 'blob' });
  }
}
