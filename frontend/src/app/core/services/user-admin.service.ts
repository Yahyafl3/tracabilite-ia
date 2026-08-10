import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { UserRole } from '../models/auth.models';

export interface ManagedUser {
  id: string;
  nom: string;
  email: string;
  role: UserRole;
  actif?: boolean;
  dateCreation?: string;
}

export interface CreateManagedUserRequest {
  nom: string;
  email: string;
  motDePasse: string;
  role: UserRole;
}

export interface UpdateManagedUserRequest {
  nom: string;
  email: string;
  motDePasse?: string;
  role: UserRole;
}

/** Ordre d’affichage du dropdown admin. Valeurs = RoleEnum backend. */
export const MANAGED_USER_ROLES: UserRole[] = [
  UserRole.AGENT_CREDIT,
  UserRole.AGENT_SANTE,
  UserRole.AGENT_PEDAGOGIQUE,
  UserRole.RESPONSABLE_CREDIT,
  UserRole.PROFESSIONNEL_SANTE,
  UserRole.RESPONSABLE_PEDAGOGIQUE,
  UserRole.AUDITEUR,
  UserRole.ADMINISTRATEUR,
];

@Injectable({ providedIn: 'root' })
export class UserAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/users`;

  list(): Observable<ManagedUser[]> {
    return this.http.get<ManagedUser[]>(this.baseUrl);
  }

  getById(id: string): Observable<ManagedUser> {
    return this.http.get<ManagedUser>(`${this.baseUrl}/${id}`);
  }

  create(request: CreateManagedUserRequest): Observable<ManagedUser> {
    return this.http.post<ManagedUser>(this.baseUrl, request);
  }

  update(id: string, request: UpdateManagedUserRequest): Observable<ManagedUser> {
    return this.http.put<ManagedUser>(`${this.baseUrl}/${id}`, request);
  }

  /** Soft-disable — historique et décisions conservés. */
  deactivate(id: string): Observable<ManagedUser> {
    return this.http.delete<ManagedUser>(`${this.baseUrl}/${id}`);
  }

  reactivate(id: string): Observable<ManagedUser> {
    return this.http.post<ManagedUser>(`${this.baseUrl}/${id}/reactivate`, {});
  }
}
