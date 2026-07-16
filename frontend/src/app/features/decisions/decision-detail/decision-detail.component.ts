import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { IconComponent } from '../../../shared/icon.component';
import { DecisionService } from '../../../core/services/decision.service';
import { ValidationService } from '../../../core/services/validation.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models/auth.models';
import { DecisionResponse, StatutDecisionEnum } from '../../../core/models/decision.models';
import {
  ConsensusResponse,
  formatConsensusDisplay,
  type ConsensusDisplay,
} from '../../../core/models/openrouter.models';
import { DecisionTraceService, DecisionHistoryEntry, DecisionSource } from '../../../core/services/decision-trace.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';

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
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Identifiant de décision manquant.');
      this.loading.set(false);
      return;
    }

    this.loadDecision(id);
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

  historyActionLabel(action: string): string {
    return action.replaceAll('_', ' ');
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

  decisionChipClass(decision?: string): string {
    if (decision === 'APPROUVER') return 'chip--approved';
    if (decision === 'REJETER') return 'chip--rejected';
    return 'chip--pending';
  }

  riskChipClass(risk?: string): string {
    if (risk === 'HIGH') return 'chip--rejected';
    if (risk === 'MEDIUM') return 'chip--modified';
    if (risk === 'LOW') return 'chip--approved';
    return 'chip--pending';
  }

  statutChipClass(statut: StatutDecisionEnum): string {
    const map: Record<StatutDecisionEnum, string> = {
      [StatutDecisionEnum.APPROUVEE]: 'chip--approved',
      [StatutDecisionEnum.MODIFIEE]: 'chip--modified',
      [StatutDecisionEnum.REJETEE]: 'chip--rejected',
      [StatutDecisionEnum.EN_ATTENTE]: 'chip--pending',
      [StatutDecisionEnum.BROUILLON]: 'chip--pending',
    };
    return map[statut] ?? 'chip--pending';
  }

  consensusDisplay(consensus: ConsensusResponse): ConsensusDisplay {
    return formatConsensusDisplay(consensus);
  }
}
