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
import { TranslatePipe } from '@ngx-translate/core';
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
import { CreditDecisionDetailsComponent } from './domain/credit-decision-details.component';
import { MedicalDecisionDetailsComponent } from './domain/medical-decision-details.component';
import { EducationDecisionDetailsComponent } from './domain/education-decision-details.component';
import {
  DOMAIN_META,
  featureDisplayName,
  resolveDomain,
  type DecisionDomain,
} from '../../../core/config/domains/domain.config';
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
import { decisionLabel, domainLabel, historyActionLabel, riskLabel, roleLabel as toRoleLabel, statutLabel } from '../../../core/utils/label.util';
import { TranslationService } from '../../../core/i18n/translation.service';

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
    CreditDecisionDetailsComponent,
    MedicalDecisionDetailsComponent,
    EducationDecisionDetailsComponent,
    TranslatePipe,
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
  private readonly i18n = inject(TranslationService);

  readonly humanDecisionOptions = computed(() => {
    this.i18n.currentLang();
    const domain = this.domain();
    return DOMAIN_META[domain].humanDecisions.map((option) => ({
      ...option,
      label: this.i18n.t(`humanDecision.${option.value}`),
    }));
  });

  readonly domain = computed<DecisionDomain>(() => resolveDomain(this.decision()?.domaine));

  readonly domainMeta = computed(() => DOMAIN_META[this.domain()]);

  readonly domainWarning = computed(() => {
    this.i18n.currentLang();
    const warning = this.i18n.t(`domainMeta.${this.domain()}.warning`);
    return warning && warning !== `domainMeta.${this.domain()}.warning` ? warning : null;
  });

  readonly agentsConsulted = computed(() => (this.decision()?.agentResponses?.length ?? 0) > 0);

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
    return role === UserRole.ADMINISTRATEUR;
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
    commentaire: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(2000)]],
    decisionHumaine: ['', Validators.required],
    accordAvecIa: [true],
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
    const domain = resolveDomain(item?.domaine);
    const expected = DOMAIN_META[domain].validatorRole as UserRole;
    const allowed =
      role === expected ||
      role === UserRole.ADMINISTRATEUR ||
      role === UserRole.RESPONSABLE_CREDIT ||
      role === UserRole.PROFESSIONNEL_SANTE ||
      role === UserRole.RESPONSABLE_PEDAGOGIQUE;
    const pending =
      item?.statutValidation === StatutDecisionEnum.EN_ATTENTE ||
      item?.statutValidation === StatutDecisionEnum.EN_ATTENTE_VALIDATION;
    return !!allowed && !!pending;
  });

  readonly isPendingValidation = computed(() => {
    const s = this.decision()?.statutValidation;
    return s === StatutDecisionEnum.EN_ATTENTE || s === StatutDecisionEnum.EN_ATTENTE_VALIDATION;
  });

  readonly tabs: Array<{ id: DetailTab; labelKey: string }> = [
    { id: 'resume', labelKey: 'decisions.detail.tabs.resume' },
    { id: 'prediction', labelKey: 'decisions.detail.tabs.prediction' },
    { id: 'shap', labelKey: 'decisions.detail.tabs.shap' },
    { id: 'agents', labelKey: 'decisions.detail.tabs.agents' },
    { id: 'validation', labelKey: 'decisions.detail.tabs.validation' },
    { id: 'history', labelKey: 'decisions.detail.tabs.history' },
    { id: 'sources', labelKey: 'decisions.detail.tabs.sources' },
    { id: 'integrity', labelKey: 'decisions.detail.tabs.integrity' },
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
        this.error.set(this.i18n.t('decisions.detail.loadError'));
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

  historyActionLabel = (action: string) => {
    this.i18n.currentLang();
    return historyActionLabel(action);
  };
  statutLabel = (statut: string) => {
    this.i18n.currentLang();
    return statutLabel(statut);
  };
  riskLabel = (risk?: string | null) => {
    this.i18n.currentLang();
    return riskLabel(risk);
  };
  domainLabel = (domain?: string | null) => {
    this.i18n.currentLang();
    return domainLabel(domain);
  };
  roleLabel = (role: string) => {
    this.i18n.currentLang();
    return toRoleLabel(role);
  };
  decisionLabel = (decision?: string | null) => {
    this.i18n.currentLang();
    return decisionLabel(decision);
  };

  reference(item: DecisionResponse): string {
    return item.reference ?? item.decisionId.slice(0, 8).toUpperCase();
  }

  formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString(this.i18n.localeId(), {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatTime(iso: string): string {
    return new Date(iso).toLocaleTimeString(this.i18n.localeId(), {
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
    return item.createdBy || '—';
  }

  validatorLabel(item: DecisionResponse): string {
    return item.validatorEmail || item.validateurRole || '—';
  }

  displayOrDash(value: unknown): string {
    if (value == null || value === '') return '—';
    return String(value);
  }

  factorLabel(name: string): string {
    return featureDisplayName(this.domain(), name);
  }

  shapImpactTooltip(impact: string): string {
    this.i18n.currentLang();
    if (impact === 'POSITIVE') return this.i18n.t('decisions.detail.impactPositiveRisk');
    if (impact === 'NEGATIVE') return this.i18n.t('decisions.detail.impactNegativeRisk');
    return impact;
  }

  shapImpactLabel(factor: { impactLabel?: string; impact: string }): string {
    this.i18n.currentLang();
    if (factor.impact === 'POSITIVE') return this.i18n.t('decisions.detail.impactPositive');
    if (factor.impact === 'NEGATIVE') return this.i18n.t('decisions.detail.impactNegative');
    return factor.impactLabel || factor.impact;
  }

  submitDomainValidation(): void {
    const id = this.decision()?.decisionId;
    if (!id || this.validationForm.invalid) {
      this.validationForm.markAllAsTouched();
      return;
    }
    const raw = this.validationForm.getRawValue();
    this.confirmAndSubmit(
      this.i18n.t('decisions.detail.confirmTitle'),
      this.i18n.t('decisions.detail.confirmBody', { decision: raw.decisionHumaine }),
      () => {
        this.submitValidation(() =>
          this.decisionService.validateDomain(id, {
            decisionFinale: raw.decisionHumaine!,
            justificationHumaine: raw.commentaire ?? undefined,
            accordAvecIa: raw.accordAvecIa ?? true,
          }),
        );
      },
    );
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



  private confirmAndSubmit(header: string, message: string, onAccept: () => void): void {
    if (!this.validationForm.controls.confirmed.value) {
      this.validationForm.controls.confirmed.markAsTouched();
      return;
    }
    this.confirmation.confirm({
      header,
      message,
      icon: 'pi pi-exclamation-circle',
      acceptLabel: this.i18n.t('common.confirm'),
      rejectLabel: this.i18n.t('common.cancel'),
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
        this.validationSuccess.set(this.i18n.t('decisions.detail.retrySuccess'));
      },
      error: (err) => {
        this.retryError.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.detail.retryError')));
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
