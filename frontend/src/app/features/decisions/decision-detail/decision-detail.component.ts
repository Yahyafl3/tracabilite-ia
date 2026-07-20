import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { filter, map } from 'rxjs';
import { Card } from 'primeng/card';
import { Tabs, TabList, Tab, TabPanels, TabPanel } from 'primeng/tabs';
import { Tag } from 'primeng/tag';
import { TableModule } from 'primeng/table';
import { Skeleton } from 'primeng/skeleton';
import { Message } from 'primeng/message';
import { Button } from 'primeng/button';
import { Textarea } from 'primeng/textarea';
import { Select } from 'primeng/select';
import { Checkbox } from 'primeng/checkbox';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { Divider } from 'primeng/divider';
import { ConfirmationService } from 'primeng/api';
import { IconComponent } from '../../../shared/icon.component';
import {
  StatusBadgeComponent,
  RiskBadgeComponent,
  EmptyStateComponent,
  ErrorStateComponent,
  LoadingSkeletonComponent,
  ConfidenceDisplayComponent,
  ConsensusCardComponent,
  AgentResponseCardComponent,
  CopyHashComponent,
  TimelineComponent,
  ModelIdentityComponent,
} from '../../../shared/ui';
import { DecisionService } from '../../../core/services/decision.service';
import { ValidationService } from '../../../core/services/validation.service';
import { AuthService } from '../../../core/services/auth.service';
import { AuditService } from '../../../core/services/audit.service';
import { UserRole } from '../../../core/models/auth.models';
import {
  DecisionResponse,
  StatutDecisionEnum,
  humanFinalLabel,
  mlConfidence,
  mlDecision,
  consensusLabel,
} from '../../../core/models/decision.models';
import {
  ConsensusResponse,
  agentByKey,
  agentDisplayName,
  agentStatusLabel,
  formatConsensusDisplay,
  type ConsensusDisplay,
} from '../../../core/models/openrouter.models';
import {
  DecisionTraceService,
  DecisionHistoryEntry,
  DecisionSource,
  DecisionSourceType,
  CreateDecisionSourceRequest,
} from '../../../core/services/decision-trace.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { decisionChipClass, riskChipClass, statutChipClass } from '../../../core/utils/chip-class.util';
import { historyActionLabel, riskLabel, statutLabel } from '../../../core/utils/label.util';

type DetailTab =
  | 'resume'
  | 'prediction'
  | 'shap'
  | 'agents'
  | 'validation'
  | 'history'
  | 'sources'
  | 'integrity';

@Component({
  selector: 'app-decision-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    ReactiveFormsModule,
    Card,
    Tabs,
    TabList,
    Tab,
    TabPanels,
    TabPanel,
    Tag,
    TableModule,
    Skeleton,
    Message,
    Button,
    Textarea,
    Select,
    Checkbox,
    ConfirmDialog,
    Divider,
    IconComponent,
    StatusBadgeComponent,
    RiskBadgeComponent,
    EmptyStateComponent,
    ErrorStateComponent,
    LoadingSkeletonComponent,
    ConfidenceDisplayComponent,
    ConsensusCardComponent,
    AgentResponseCardComponent,
    CopyHashComponent,
    TimelineComponent,
    ModelIdentityComponent,
  ],
  providers: [ConfirmationService],
  templateUrl: './decision-detail.component.html',
  styleUrl: './decision-detail.component.scss',
})
export class DecisionDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly decisionService = inject(DecisionService);
  private readonly validationService = inject(ValidationService);
  private readonly authService = inject(AuthService);
  private readonly traceService = inject(DecisionTraceService);
  private readonly auditService = inject(AuditService);
  private readonly confirmation = inject(ConfirmationService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly humanDecisionOptions = [
    { label: 'APPROUVER', value: 'APPROUVER' },
    { label: 'REJETER', value: 'REJETER' },
  ];

  readonly decision = signal<DecisionResponse | null>(null);
  readonly historyEntries = signal<DecisionHistoryEntry[]>([]);
  readonly sources = signal<DecisionSource[]>([]);
  readonly traceLoading = signal(false);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly validationLoading = signal(false);
  readonly validationError = signal<string | null>(null);
  readonly validationSuccess = signal<string | null>(null);
  readonly activeTab = signal<DetailTab>('resume');
  readonly retryLoading = signal(false);
  readonly retryError = signal<string | null>(null);
  readonly auditLoading = signal(false);
  readonly auditError = signal<string | null>(null);
  readonly integrityValid = signal<boolean | null>(null);
  readonly sourceLoading = signal(false);
  readonly sourceError = signal<string | null>(null);

  readonly sourceTypes: DecisionSourceType[] = [
    'USER_INPUT',
    'BUSINESS_DATA',
    'DOCUMENT',
    'URL',
    'DATABASE',
    'MODEL_OUTPUT',
    'OTHER',
  ];

  readonly isAdmin = computed(() =>
    this.authService.currentUser?.role === UserRole.ADMINISTRATEUR,
  );

  readonly canAudit = computed(() => {
    const role = this.authService.currentUser?.role;
    return role === UserRole.ADMINISTRATEUR || role === UserRole.AUDITEUR;
  });

  readonly canManageSources = computed(() => {
    const role = this.authService.currentUser?.role;
    return role === UserRole.VALIDATEUR || role === UserRole.ADMINISTRATEUR;
  });

  readonly hasRetryableAgents = computed(() =>
    (this.decision()?.agentResponses ?? []).some((agent) => {
      if (agent.statut === 'SUCCESS') {
        return false;
      }
      if (['RATE_LIMITED', 'TIMEOUT', 'TEMPORARILY_UNAVAILABLE'].includes(agent.fallbackReason ?? '')) {
        return true;
      }
      if (agent.statut === 'TIMEOUT') {
        return true;
      }
      return ['OPENROUTER_RATE_LIMITED', 'OPENROUTER_TIMEOUT', 'OPENROUTER_UNAVAILABLE'].includes(
        agent.codeErreur ?? '',
      );
    }),
  );

  readonly validationForm = this.fb.group({
    commentaire: ['', Validators.maxLength(2000)],
    decisionHumaine: ['REJETER' as 'APPROUVER' | 'REJETER', Validators.required],
    confirmed: [false, Validators.requiredTrue],
  });

  readonly sourceForm = this.fb.group({
    sourceType: ['DOCUMENT' as DecisionSourceType, Validators.required],
    name: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', Validators.maxLength(2000)],
    url: ['', Validators.maxLength(2048)],
    documentReference: ['', Validators.maxLength(512)],
  });

  readonly canValidate = computed(() => {
    const item = this.decision();
    const role = this.authService.currentUser?.role;
    const isValidator = role === UserRole.VALIDATEUR || role === UserRole.ADMINISTRATEUR;
    return isValidator && item?.statutValidation === StatutDecisionEnum.EN_ATTENTE;
  });

  readonly isPendingValidation = computed(() =>
    this.decision()?.statutValidation === StatutDecisionEnum.EN_ATTENTE,
  );

  readonly tabs: Array<{ id: DetailTab; label: string }> = [
    { id: 'resume', label: 'Résumé' },
    { id: 'prediction', label: 'ML' },
    { id: 'shap', label: 'SHAP' },
    { id: 'agents', label: 'Agents IA' },
    { id: 'validation', label: 'Validation humaine' },
    { id: 'history', label: 'Historique' },
    { id: 'sources', label: 'Sources' },
    { id: 'integrity', label: 'Intégrité' },
  ];

  constructor() {
    this.route.paramMap
      .pipe(
        map((params) => params.get('id')),
        filter((id): id is string => !!id),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((id) => {
        this.loading.set(true);
        this.error.set(null);
        this.validationSuccess.set(null);
        this.validationError.set(null);
        this.auditError.set(null);
        this.integrityValid.set(null);
        this.activeTab.set('resume');
        this.loadDecision(id);
      });
  }

  loadDecision(id: string): void {
    this.decisionService.getById(id).subscribe({
      next: (response) => {
        this.decision.set(response);
        this.loading.set(false);
        this.loadTraceData(id);
      },
      error: () => {
        this.error.set('Impossible de charger la décision.');
        this.loading.set(false);
      },
    });
  }

  private loadTraceData(id: string): void {
    this.traceLoading.set(true);
    this.traceService.getHistory(id).subscribe({
      next: (entries) => this.historyEntries.set(entries),
      error: () => this.historyEntries.set([]),
    });
    this.traceService.getSources(id).subscribe({
      next: (items) => {
        this.sources.set(items);
        this.traceLoading.set(false);
      },
      error: () => {
        this.sources.set([]);
        this.traceLoading.set(false);
      },
    });
  }

  historyActionLabel = historyActionLabel;
  statutLabel = statutLabel;
  riskLabel = riskLabel;

  reference(item: DecisionResponse): string {
    return item.reference ?? item.decisionId.slice(0, 8).toUpperCase();
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  }

  featureValue(item: DecisionResponse, key: string): unknown {
    return item.features?.[key];
  }

  formatCurrency(value: unknown): string {
    if (value == null || value === '') return '—';
    const num = Number(value);
    if (Number.isNaN(num)) return String(value);
    return `${num.toLocaleString('fr-FR')} €`;
  }

  formatFeature(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value);
  }

  creatorLabel(item: DecisionResponse): string {
    return item.validatorEmail || '—';
  }

  shapImpactTooltip(impact: string): string {
    if (impact === 'POSITIVE') return 'Favorise APPROUVER';
    if (impact === 'NEGATIVE') return 'Favorise REJETER';
    return impact;
  }

  shapImpactLabel(factor: { impactLabel?: string; impact: string }): string {
    if (factor.impact === 'POSITIVE') return 'Favorise APPROUVER';
    if (factor.impact === 'NEGATIVE') return 'Favorise REJETER';
    return factor.impactLabel || factor.impact;
  }

  tabPanelId(tab: DetailTab): string {
    return `decision-tabpanel-${tab}`;
  }

  setTab(tab: DetailTab | string | number | undefined): void {
    if (tab == null) return;
    const next = String(tab) as DetailTab;
    this.activeTab.set(next);
    if (next === 'integrity' && this.canAudit()) {
      this.loadIntegrityAudit();
    }
  }

  onTabsValueChange(value: string | number | undefined): void {
    this.setTab(value);
  }

  onTabKeydown(event: KeyboardEvent, index: number): void {
    const keys = ['ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (!keys.includes(event.key)) return;

    event.preventDefault();
    let next = index;

    if (event.key === 'ArrowLeft') {
      next = index <= 0 ? this.tabs.length - 1 : index - 1;
    } else if (event.key === 'ArrowRight') {
      next = index >= this.tabs.length - 1 ? 0 : index + 1;
    } else if (event.key === 'Home') {
      next = 0;
    } else if (event.key === 'End') {
      next = this.tabs.length - 1;
    }

    this.setTab(this.tabs[next].id);
    queueMicrotask(() => {
      const el = document.getElementById(`decision-tab-${this.tabs[next].id}`);
      el?.focus();
    });
  }

  loadIntegrityAudit(): void {
    const id = this.decision()?.decisionId;
    if (!id || !this.canAudit()) return;

    this.auditLoading.set(true);
    this.auditError.set(null);

    this.auditService.getDecisionAudit(id).subscribe({
      next: (audit) => {
        this.integrityValid.set(audit.integrityValid);
        if (audit.currentHash) {
          this.decision.update((current) =>
            current ? { ...current, currentHash: audit.currentHash } : current,
          );
        }
        this.auditLoading.set(false);
      },
      error: (err) => {
        this.auditError.set(resolveHttpErrorMessage(err, 'Impossible de vérifier l\'intégrité.'));
        this.auditLoading.set(false);
      },
    });
  }

  verifyIntegrity(): void {
    this.loadIntegrityAudit();
  }

  approve(): void {
    this.confirmAndSubmit(
      'Confirmer l’approbation',
      'Enregistrer APPROUVER sur le dossier global de décision ?',
      () => {
        const id = this.decision()?.decisionId;
        if (!id) return;
        this.submitValidation(() =>
          this.validationService.approve(id, {
            commentaire: this.validationForm.value.commentaire ?? undefined,
          }),
        );
      },
    );
  }

  reject(): void {
    this.confirmAndSubmit(
      'Confirmer le rejet',
      'Enregistrer REJETER sur le dossier global de décision ?',
      () => {
        const id = this.decision()?.decisionId;
        if (!id) return;
        this.submitValidation(() =>
          this.validationService.reject(id, {
            commentaire: this.validationForm.value.commentaire ?? undefined,
          }),
        );
      },
    );
  }

  modify(): void {
    this.confirmAndSubmit(
      'Confirmer la modification',
      'Enregistrer la décision humaine finale (MODIFIER) sur le dossier global ?',
      () => {
        const id = this.decision()?.decisionId;
        if (!id || this.validationForm.invalid) {
          this.validationForm.markAllAsTouched();
          return;
        }
        this.submitValidation(() =>
          this.validationService.modify(id, {
            commentaire: this.validationForm.value.commentaire ?? undefined,
            decisionHumaine: this.validationForm.value.decisionHumaine as 'APPROUVER' | 'REJETER',
          }),
        );
      },
    );
  }

  review(): void {
    this.confirmAndSubmit(
      'Confirmer la revue',
      'Enregistrer REVIEW sur le dossier global de décision ?',
      () => {
        const id = this.decision()?.decisionId;
        if (!id) return;
        this.submitValidation(() =>
          this.validationService.review(id, {
            commentaire: this.validationForm.value.commentaire ?? undefined,
          }),
        );
      },
    );
  }

  private confirmAndSubmit(header: string, message: string, onAccept: () => void): void {
    if (!this.validationForm.controls.confirmed.value) {
      this.validationForm.controls.confirmed.markAsTouched();
      return;
    }
    this.confirmation.confirm({
      header,
      message,
      icon: 'pi pi-exclamation-circle',
      acceptLabel: 'Confirmer',
      rejectLabel: 'Annuler',
      acceptButtonStyleClass: 'p-button-primary',
      rejectButtonStyleClass: 'p-button-text',
      accept: onAccept,
    });
  }

  decisionTagSeverity(value: string | undefined): 'success' | 'danger' | 'warn' | 'secondary' {
    if (value === 'APPROUVER' || value === 'APPROUVEE') return 'success';
    if (value === 'REJETER' || value === 'REJETEE') return 'danger';
    if (value === 'REVIEW' || value === 'MODIFIEE') return 'warn';
    return 'secondary';
  }

  retryFailedAgents(): void {
    const id = this.decision()?.decisionId;
    if (!id) return;
    this.retryLoading.set(true);
    this.retryError.set(null);
    this.decisionService.retryFailedAgents(id).subscribe({
      next: (response) => {
        this.decision.set(response);
        this.retryLoading.set(false);
        this.validationSuccess.set('Agents IA relancés avec succès.');
      },
      error: (err) => {
        this.retryError.set(resolveHttpErrorMessage(err, 'Impossible de relancer les agents.'));
        this.retryLoading.set(false);
      },
    });
  }

  addSource(): void {
    const id = this.decision()?.decisionId;
    if (!id || this.sourceForm.invalid) {
      this.sourceForm.markAllAsTouched();
      return;
    }

    const raw = this.sourceForm.getRawValue();
    const request: CreateDecisionSourceRequest = {
      sourceType: raw.sourceType!,
      name: raw.name!.trim(),
      description: raw.description?.trim() || undefined,
      url: raw.url?.trim() || undefined,
      documentReference: raw.documentReference?.trim() || undefined,
    };

    this.sourceLoading.set(true);
    this.sourceError.set(null);

    this.traceService.addSource(id, request).subscribe({
      next: (source) => {
        this.sources.update((items) => [...items, source]);
        this.sourceForm.reset({
          sourceType: 'DOCUMENT',
          name: '',
          description: '',
          url: '',
          documentReference: '',
        });
        this.sourceLoading.set(false);
      },
      error: (err) => {
        this.sourceError.set(resolveHttpErrorMessage(err, 'Impossible d\'ajouter la source.'));
        this.sourceLoading.set(false);
      },
    });
  }

  removeSource(sourceId: string): void {
    const id = this.decision()?.decisionId;
    if (!id) return;

    this.traceService.removeSource(id, sourceId).subscribe({
      next: () => {
        this.sources.update((items) => items.filter((s) => s.sourceId !== sourceId));
      },
      error: (err) => {
        this.sourceError.set(resolveHttpErrorMessage(err, 'Impossible de supprimer la source.'));
      },
    });
  }

  sourceRef(source: DecisionSource): string {
    return source.url || source.documentReference || '—';
  }

  private submitValidation(action: () => import('rxjs').Observable<DecisionResponse>): void {
    this.validationLoading.set(true);
    this.validationError.set(null);
    this.validationSuccess.set(null);

    action().subscribe({
      next: (response) => {
        this.decision.set(response);
        this.validationLoading.set(false);
        this.validationSuccess.set('Validation enregistrée avec succès.');
        this.validationForm.patchValue({ confirmed: false });
      },
      error: (err) => {
        this.validationError.set(resolveHttpErrorMessage(err, 'Erreur lors de la validation.'));
        this.validationLoading.set(false);
      },
    });
  }

  featureEntries(decision: DecisionResponse): Array<{ key: string; value: unknown }> {
    return Object.entries(decision.features ?? {}).map(([key, value]) => ({ key, value }));
  }

  decisionChipClass = decisionChipClass;
  riskChipClass = riskChipClass;
  statutChipClass = statutChipClass;

  consensusDisplay(consensus: ConsensusResponse): ConsensusDisplay {
    return formatConsensusDisplay(consensus);
  }

  agentName = agentDisplayName;
  agentForKey = agentByKey;
  agentStatus = agentStatusLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;
  consensusLabel = consensusLabel;
  humanFinalLabel = humanFinalLabel;
}
