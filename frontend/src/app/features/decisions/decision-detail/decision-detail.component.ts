import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { filter, map } from 'rxjs';
import { IconComponent } from '../../../shared/icon.component';
import { DecisionService } from '../../../core/services/decision.service';
import { ValidationService } from '../../../core/services/validation.service';
import { AuthService } from '../../../core/services/auth.service';
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
  formatDeclaredConfidence,
  formatConsensusDisplay,
  agentFallbackMessage,
  type ConsensusDisplay,
} from '../../../core/models/openrouter.models';
import { DecisionTraceService, DecisionHistoryEntry, DecisionSource } from '../../../core/services/decision-trace.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { decisionChipClass, riskChipClass, statutChipClass } from '../../../core/utils/chip-class.util';
import { historyActionLabel, riskLabel, statutLabel } from '../../../core/utils/label.util';

type DetailTab = 'resume' | 'prediction' | 'explain' | 'agents' | 'validation' | 'history' | 'sources';

@Component({
  selector: 'app-decision-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, IconComponent],
  templateUrl: './decision-detail.component.html',
  styleUrl: './decision-detail.component.scss',
})
export class DecisionDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly decisionService = inject(DecisionService);
  private readonly validationService = inject(ValidationService);
  private readonly authService = inject(AuthService);
  private readonly traceService = inject(DecisionTraceService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

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

  readonly isAdmin = computed(() =>
    this.authService.currentUser?.role === UserRole.ADMINISTRATEUR,
  );

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
      return ['OPENROUTER_RATE_LIMITED', 'OPENROUTER_TIMEOUT', 'OPENROUTER_UNAVAILABLE'].includes(agent.codeErreur ?? '');
    }),
  );

  readonly validationForm = this.fb.group({
    commentaire: ['', Validators.maxLength(2000)],
    decisionHumaine: ['REJETER' as 'APPROUVER' | 'REJETER', Validators.required],
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
    { id: 'prediction', label: 'Prédiction ML' },
    { id: 'explain', label: 'Explicabilité SHAP' },
    { id: 'agents', label: 'Agents OpenRouter' },
    { id: 'validation', label: 'Validation humaine' },
    { id: 'history', label: 'Historique' },
    { id: 'sources', label: 'Sources' },
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

  historyItemClass(action: string): string {
    if (action.includes('FAILED') || action.includes('REJECTED')) return 'history-item--danger';
    if (action.includes('APPROVED') || action.includes('COMPLETED') || action.includes('SUCCESS') || action.includes('VERIFIED')) {
      return 'history-item--success';
    }
    if (action.includes('MODIFIED') || action.includes('REVIEW')) return 'history-item--warning';
    return '';
  }

  setTab(tab: DetailTab): void {
    this.activeTab.set(tab);
  }

  approve(): void {
    const id = this.decision()?.decisionId;
    if (!id) return;
    this.submitValidation(() =>
      this.validationService.approve(id, { commentaire: this.validationForm.value.commentaire ?? undefined }),
    );
  }

  reject(): void {
    const id = this.decision()?.decisionId;
    if (!id) return;
    this.submitValidation(() =>
      this.validationService.reject(id, { commentaire: this.validationForm.value.commentaire ?? undefined }),
    );
  }

  modify(): void {
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
  }

  review(): void {
    const id = this.decision()?.decisionId;
    if (!id) return;
    this.submitValidation(() =>
      this.validationService.review(id, { commentaire: this.validationForm.value.commentaire ?? undefined }),
    );
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
        this.validationSuccess.set('Agents OpenRouter relancés avec succès.');
      },
      error: (err) => {
        this.retryError.set(resolveHttpErrorMessage(err, 'Impossible de relancer les agents.'));
        this.retryLoading.set(false);
      },
    });
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
  agentFallback = agentFallbackMessage;
  declaredConfidenceLabel = formatDeclaredConfidence;
  agentForKey = agentByKey;
  agentStatus = agentStatusLabel;
  mlDecision = mlDecision;
  mlConfidence = mlConfidence;
  consensusLabel = consensusLabel;
  humanFinalLabel = humanFinalLabel;
}
