import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { TableModule } from 'primeng/table';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { Divider } from 'primeng/divider';
import { Textarea } from 'primeng/textarea';
import { Select } from 'primeng/select';
import { Tooltip } from 'primeng/tooltip';
import { ValidationService } from '../../core/services/validation.service';
import { DecisionResponse, consensusLabel, mlDecision, mlConfidence } from '../../core/models/decision.models';
import { decisionLabel, riskLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { ConfidenceDisplayComponent } from '../../shared/ui';

type ActionType = 'APPROUVER' | 'REJETER' | 'MODIFIER' | 'REVIEW';

@Component({
  selector: 'app-validation-queue',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    TableModule,
    Tag,
    Button,
    Dialog,
    Message,
    Skeleton,
    Divider,
    Textarea,
    Select,
    Tooltip,
    ConfidenceDisplayComponent,
  ],
  templateUrl: './validation-queue.component.html',
  styleUrl: './validation-queue.component.scss',
})
export class ValidationQueueComponent {
  private readonly validationService = inject(ValidationService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  // ── State ────────────────────────────────────────────────
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);
  readonly submitSuccess = signal<string | null>(null);

  // ── Action Dialog ────────────────────────────────────────
  readonly actionDialogVisible = signal(false);
  readonly actionTarget = signal<DecisionResponse | null>(null);
  readonly pendingAction = signal<ActionType | null>(null);

  readonly actionForm = this.fb.group({
    commentaire: this.fb.nonNullable.control('', [Validators.required, Validators.minLength(10)]),
    decisionHumaine: this.fb.nonNullable.control('' as 'APPROUVER' | 'REJETER' | ''),
  });

  readonly newDecisionOptions = [
    { label: 'Approuver', value: 'APPROUVER' },
    { label: 'Rejeter', value: 'REJETER' },
  ];

  // ── Detail Dialog ────────────────────────────────────────
  readonly detailVisible = signal(false);
  readonly detailTarget = signal<DecisionResponse | null>(null);

  // ── Computed KPIs ────────────────────────────────────────
  readonly kpis = computed(() => {
    const all = this.decisions();
    const high = all.filter((d) => d.riskLevel === 'HIGH').length;
    const withConsensus = all.filter(
      (d) => d.consensusDecision && d.consensusDecision !== 'NO_CONSENSUS' && d.consensusDecision !== 'INSUFFICIENT_RESPONSES'
    ).length;
    const consensusRate = all.length > 0 ? Math.round((withConsensus / all.length) * 100) : 0;

    return [
      {
        label: 'En attente',
        value: String(this.totalElements()),
        icon: 'pi pi-clock',
        color: 'warn',
        hint: 'Décisions à valider',
      },
      {
        label: 'Risque élevé',
        value: String(high),
        icon: 'pi pi-exclamation-triangle',
        color: high > 0 ? 'danger' : 'success',
        hint: 'Nécessitent attention',
      },
      {
        label: 'Avec consensus',
        value: `${consensusRate}%`,
        icon: 'pi pi-check-circle',
        color: 'success',
        hint: `${withConsensus} / ${all.length} dossiers`,
      },
      {
        label: 'Chargée',
        value: String(all.length),
        icon: 'pi pi-list',
        color: 'info',
        hint: 'Dossiers dans la file',
      },
    ];
  });

  constructor() {
    this.loadPending();
  }

  // ── Data Loading ─────────────────────────────────────────
  loadPending(): void {
    this.loading.set(true);
    this.error.set(null);
    this.validationService.getPending(0, 50).subscribe({
      next: (page) => {
        this.decisions.set(page.content);
        this.totalElements.set(page.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger la file de validation.'));
        this.loading.set(false);
      },
    });
  }

  // ── Detail Dialog ────────────────────────────────────────
  openDetail(row: DecisionResponse): void {
    this.detailTarget.set(row);
    this.detailVisible.set(true);
  }

  closeDetail(): void {
    this.detailVisible.set(false);
    this.detailTarget.set(null);
  }

  goToDossier(row: DecisionResponse): void {
    this.closeDetail();
    void this.router.navigate(['/decisions', row.decisionId]);
  }

  // ── Action Dialog ────────────────────────────────────────
  openAction(row: DecisionResponse, action: ActionType): void {
    this.actionTarget.set(row);
    this.pendingAction.set(action);
    this.actionForm.reset({ commentaire: '', decisionHumaine: '' });
    this.submitError.set(null);
    this.submitSuccess.set(null);
    this.actionDialogVisible.set(true);
  }

  closeAction(): void {
    this.actionDialogVisible.set(false);
    this.actionTarget.set(null);
    this.pendingAction.set(null);
  }

  submitAction(): void {
    if (this.actionForm.invalid) {
      this.actionForm.markAllAsTouched();
      return;
    }

    const row = this.actionTarget();
    const action = this.pendingAction();
    if (!row || !action) return;

    const { commentaire, decisionHumaine } = this.actionForm.getRawValue();
    const request = {
      commentaire,
      decisionHumaine: decisionHumaine || undefined,
    };

    this.submitting.set(true);
    this.submitError.set(null);

    let call$;
    if (action === 'APPROUVER') {
      call$ = this.validationService.approve(row.decisionId, request);
    } else if (action === 'REJETER') {
      call$ = this.validationService.reject(row.decisionId, request);
    } else if (action === 'MODIFIER') {
      call$ = this.validationService.modify(row.decisionId, request);
    } else {
      call$ = this.validationService.review(row.decisionId, request);
    }

    call$.subscribe({
      next: () => {
        this.submitting.set(false);
        this.closeAction();
        // Remove from list
        this.decisions.update((list) => list.filter((d) => d.decisionId !== row.decisionId));
        this.totalElements.update((n) => Math.max(0, n - 1));
      },
      error: (err) => {
        this.submitting.set(false);
        this.submitError.set(resolveHttpErrorMessage(err, 'Erreur lors de la validation.'));
      },
    });
  }

  // ── Helpers ──────────────────────────────────────────────
  actionLabel(action: ActionType | null): string {
    if (action === 'APPROUVER') return 'Approuver';
    if (action === 'REJETER') return 'Rejeter';
    if (action === 'MODIFIER') return 'Modifier la décision';
    if (action === 'REVIEW') return 'Demander une revue';
    return 'Valider';
  }

  actionSeverity(action: ActionType | null): 'success' | 'danger' | 'warn' | 'secondary' {
    if (action === 'APPROUVER') return 'success';
    if (action === 'REJETER') return 'danger';
    if (action === 'MODIFIER') return 'warn';
    return 'secondary';
  }

  actionIcon(action: ActionType | null): string {
    if (action === 'APPROUVER') return 'pi pi-check';
    if (action === 'REJETER') return 'pi pi-times';
    if (action === 'MODIFIER') return 'pi pi-pencil';
    return 'pi pi-refresh';
  }

  hasError(field: string): boolean {
    const ctrl = this.actionForm.get(field);
    return !!ctrl && ctrl.invalid && ctrl.touched;
  }
  decisionLabel = decisionLabel;
  riskLabel = riskLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;
  consensusLabel = consensusLabel;

  mlSeverity(value: string | undefined): 'success' | 'danger' | 'secondary' | 'warn' {
    if (value === 'APPROUVER' || value === 'APPROUVEE') return 'success';
    if (value === 'REJETER' || value === 'REJETEE') return 'danger';
    if (value === 'REVIEW' || value === 'MODIFIEE') return 'warn';
    return 'secondary';
  }

  riskSeverity(risk: string | undefined): 'success' | 'warn' | 'danger' | 'secondary' {
    if (risk === 'LOW') return 'success';
    if (risk === 'MEDIUM') return 'warn';
    if (risk === 'HIGH') return 'danger';
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
}
