import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { Tooltip } from 'primeng/tooltip';
import { DecisionService } from '../../core/services/decision.service';
import { DecisionResponse, mlDecision, mlConfidence } from '../../core/models/decision.models';
import { decisionLabel, riskLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { ConfidenceDisplayComponent } from '../../shared/ui';

@Component({
  selector: 'app-validation-queue',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    Card,
    TableModule,
    Tag,
    Button,
    Message,
    Skeleton,
    Tooltip,
    ConfidenceDisplayComponent,
  ],
  templateUrl: './validation-queue.component.html',
  styleUrl: './validation-queue.component.scss',
})
export class ValidationQueueComponent {
  private readonly decisionService = inject(DecisionService);
  private readonly router = inject(Router);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);

  readonly kpis = computed(() => {
    const all = this.decisions();
    const high = all.filter((d) => {
      const r = (d.riskLevel || '').toUpperCase();
      return r.includes('ELEVE') || r === 'HIGH';
    }).length;
    return [
      { label: 'En attente', value: String(this.totalElements()), icon: 'pi pi-clock', color: 'warn' },
      { label: 'Risque élevé', value: String(high), icon: 'pi pi-exclamation-triangle', color: high > 0 ? 'danger' : 'success' },
      { label: 'Crédit', value: String(all.filter((d) => (d.domaine || 'CREDIT') === 'CREDIT').length), icon: 'pi pi-wallet', color: 'info' },
      { label: 'Médical', value: String(all.filter((d) => d.domaine === 'MEDICAL').length), icon: 'pi pi-heart', color: 'warn' },
      { label: 'Éducation', value: String(all.filter((d) => d.domaine === 'EDUCATION').length), icon: 'pi pi-book', color: 'success' },
    ];
  });

  constructor() {
    this.loadPending();
  }

  loadPending(): void {
    this.loading.set(true);
    this.error.set(null);
    this.decisionService.pendingValidation().subscribe({
      next: (list) => {
        this.decisions.set(list);
        this.totalElements.set(list.length);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger la file de validation.'));
        this.loading.set(false);
      },
    });
  }

  openDetail(row: DecisionResponse): void {
    void this.router.navigate(['/validation', row.decisionId]);
  }

  openValidate(row: DecisionResponse): void {
    void this.router.navigate(['/validation', row.decisionId]);
  }

  submitForReview(row: DecisionResponse): void {
    void this.router.navigate(['/validation', row.decisionId]);
  }

  domainBadge(row: DecisionResponse): string {
    return row.domaine || 'CREDIT';
  }

  domainSeverity(domain: string): 'info' | 'success' | 'warn' | 'secondary' {
    if (domain === 'MEDICAL') return 'warn';
    if (domain === 'EDUCATION') return 'success';
    if (domain === 'CREDIT') return 'info';
    return 'secondary';
  }

  decisionLabel = decisionLabel;
  riskLabel = riskLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;

  mlSeverity(value: string | undefined): 'success' | 'danger' | 'secondary' | 'warn' {
    if (!value) return 'secondary';
    if (value.includes('FAIBLE') || value === 'APPROUVER' || value.includes('ACCEPT')
      || value.includes('SUIVI') || value.includes('AUCUNE')) return 'success';
    if (value.includes('MOYEN') || value.includes('MODERE') || value.includes('ACCOMP')) return 'warn';
    if (value.includes('ELEVE') || value === 'REJETER' || value.includes('REFUS')
      || value.includes('ORIENTATION_SPEC') || value.includes('DECROCHAGE')) return 'danger';
    return 'warn';
  }

  riskSeverity(risk: string | undefined): 'success' | 'warn' | 'danger' | 'secondary' {
    if (!risk) return 'secondary';
    const r = risk.toUpperCase();
    if (r.includes('FAIBLE') || r === 'LOW') return 'success';
    if (r.includes('MOYEN') || r.includes('MODERE') || r === 'MEDIUM') return 'warn';
    if (r.includes('ELEVE') || r === 'HIGH') return 'danger';
    return 'secondary';
  }

  kpiColorClass(color: string): string {
    const map: Record<string, string> = {
      warn: 'kpi--warn',
      danger: 'kpi--danger',
      success: 'kpi--success',
      info: 'kpi--info',
    };
    return map[color] ?? 'kpi--info';
  }

  reference(row: DecisionResponse): string {
    return row.dossierReference ?? row.reference ?? row.decisionId.slice(0, 8).toUpperCase();
  }
}
