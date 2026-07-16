import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ComparaisonAgent {
  rang: number;
  systemeIaId?: string;
  nom: string;
  fournisseur: string;
  modele: string;
  versionModele: string;
  totalDecisions: number;
  approuvees: number;
  modifiees: number;
  rejetees: number;
  enAttente: number;
  scorePourcentage: number;
}

@Injectable({ providedIn: 'root' })
export class ComparaisonService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/comparaison`;

  getOpenRouterAgents(): Observable<ComparaisonAgent[]> {
    return this.http.get<ComparaisonAgent[]>(this.baseUrl);
  }
}
