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
import { Checkbox } from 'primeng/checkbox';
import { DecisionService } from '../../core/services/decision.service';
import { AuthService } from '../../core/services/auth.service';
import { DecisionResponse, mlDecision, mlConfidence } from '../../core/models/decision.models';
import { UserRole } from '../../core/models/auth.models';
import { decisionLabel, riskLabel } from '../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../core/utils/http-error.util';
import { ConfidenceDisplayComponent } from '../../shared/ui';
import { DOMAIN_META, DecisionDomain } from '../../core/config/domains/domain.config';

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
    Checkbox,
    ConfidenceDisplayComponent,
  ],
  templateUrl: './validation-queue.component.html',
  styleUrl: './validation-queue.component.scss',
})
export class ValidationQueueComponent {
  private readonly decisionService = inject(DecisionService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  /** Domain allowed for the current user's role. null = admin/auditor (all domains). */
  private readonly allowedDomain = computed<DecisionDomain | null>(() => {
    const role = this.authService.currentUser?.role as UserRole | undefined;
    if (role === UserRole.RESPONSABLE_CREDIT || role === UserRole.VALIDATEUR) return 'CREDIT';
    if (role === UserRole.PROFESSIONNEL_SANTE) return 'MEDICAL';
    if (role === UserRole.RESPONSABLE_PEDAGOGIQUE) return 'EDUCATION';
    return null; // ADMIN / AUDITEUR see all
  });

  /** Label for the "Examiner" button, domain-specific. */
  readonly examinerLabel = computed(() => {
    const d = this.allowedDomain();
    if (d === 'CREDIT') return 'Examiner le dossier crédit';
    if (d === 'MEDICAL') return 'Examiner le dossier médical';
    if (d === 'EDUCATION') return 'Examiner le dossier pédagogique';
    return 'Examiner le dossier';
  });

  /**
   * Returns true when the current user may examine this specific row:
   * - status must be EN_ATTENTE_VALIDATION
   * - role must match the decision's domain
   */
  canExamine(row: DecisionResponse): boolean {
    if (row.statutValidation !== 'EN_ATTENTE_VALIDATION') return false;
    const d = this.allowedDomain();
    if (d === null) return true; // admin/auditor
    const rowDomain = (row.domaine || 'CREDIT') as DecisionDomain;
    return rowDomain === d;
  }

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decisions = signal<DecisionResponse[]>([]);
  readonly totalElements = signal(0);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);

  readonly actionDialogVisible = signal(false);
  readonly actionTarget = signal<DecisionResponse | null>(null);

  readonly actionForm = this.fb.group({
    decisionFinale: this.fb.nonNullable.control('', [Validators.required]),
    justificationHumaine: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(10),
    ]),
    accordAvecIa: this.fb.nonNullable.control(true),
  });

  readonly detailVisible = signal(false);
  readonly detailTarget = signal<DecisionResponse | null>(null);

  readonly decisionOptions = computed(() => {
    const row = this.actionTarget();
    const domain = (row?.domaine || 'CREDIT') as DecisionDomain;
    return DOMAIN_META[domain]?.humanDecisions ?? [];
  });

  readonly medicalWarning = computed(() => {
    const domain = this.actionTarget()?.domaine || this.detailTarget()?.domaine;
    return domain === 'MEDICAL'
      ? 'Cette estimation ne constitue pas un diagnostic médical et doit être revue par un professionnel de santé.'
      : null;
  });

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
    this.actionForm.get('accordAvecIa')?.valueChanges.subscribe((agree) => {
      const ctrl = this.actionForm.get('justificationHumaine');
      if (!agree) {
        ctrl?.setValidators([Validators.required, Validators.minLength(30)]);
      } else {
        ctrl?.setValidators([Validators.required, Validators.minLength(10)]);
      }
      ctrl?.updateValueAndValidity();
    });
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

  openValidate(row: DecisionResponse): void {
    this.actionTarget.set(row);
    this.actionForm.reset({
      decisionFinale: '',
      justificationHumaine: '',
      accordAvecIa: true,
    });
    this.submitError.set(null);
    this.actionDialogVisible.set(true);
  }

  closeAction(): void {
    this.actionDialogVisible.set(false);
    this.actionTarget.set(null);
  }

  submitAction(): void {
    if (this.actionForm.invalid) {
      this.actionForm.markAllAsTouched();
      return;
    }
    const row = this.actionTarget();
    if (!row) return;

    const { decisionFinale, justificationHumaine, accordAvecIa } = this.actionForm.getRawValue();
    this.submitting.set(true);
    this.submitError.set(null);

    this.decisionService
      .validateDomain(row.decisionId, {
        decisionFinale,
        justificationHumaine,
        accordAvecIa,
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.closeAction();
          this.decisions.update((list) => list.filter((d) => d.decisionId !== row.decisionId));
          this.totalElements.update((n) => Math.max(0, n - 1));
        },
        error: (err) => {
          this.submitting.set(false);
          this.submitError.set(resolveHttpErrorMessage(err, 'Erreur lors de la validation.'));
        },
      });
  }

  submitForReview(row: DecisionResponse): void {
    this.openValidate(row);
    this.actionForm.patchValue({ decisionFinale: 'A_REVOIR', accordAvecIa: false });
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

  hasError(field: string): boolean {
    const ctrl = this.actionForm.get(field);
    return !!ctrl && ctrl.invalid && ctrl.touched;
  }

  decisionLabel = decisionLabel;
  riskLabel = riskLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;

  mlSeverity(value: string | undefined): 'success' | 'danger' | 'secondary' | 'warn' {
    if (!value) return 'secondary';
    if (value.includes('FAIBLE') || value === 'APPROUVER') return 'success';
    if (value.includes('MOYEN') || value.includes('MODERE')) return 'warn';
    if (value.includes('ELEVE') || value === 'REJETER') return 'danger';
    return 'secondary';
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
