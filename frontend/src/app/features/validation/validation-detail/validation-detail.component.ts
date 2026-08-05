import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Card } from 'primeng/card';
import { Tag } from 'primeng/tag';
import { Button } from 'primeng/button';
import { Divider } from 'primeng/divider';
import { Message } from 'primeng/message';
import { Skeleton } from 'primeng/skeleton';
import { Select } from 'primeng/select';
import { Checkbox } from 'primeng/checkbox';
import { Textarea } from 'primeng/textarea';
import { DecisionService } from '../../../core/services/decision.service';
import { AuthService } from '../../../core/services/auth.service';
import { DecisionResponse, ExplanationFactor } from '../../../core/models/decision.models';
import { DOMAIN_META, DecisionDomain, featureDisplayName, resolveDomain } from '../../../core/config/domains/domain.config';
import { decisionLabel, riskLabel } from '../../../core/utils/label.util';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import {
  ConfidenceDisplayComponent,
  RiskBadgeComponent,
  AgentResponseCardComponent,
} from '../../../shared/ui';

@Component({
  selector: 'app-validation-detail',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    Tag,
    Button,
    Divider,
    Message,
    Skeleton,
    Select,
    Checkbox,
    Textarea,
    ConfidenceDisplayComponent,
    RiskBadgeComponent,
    AgentResponseCardComponent,
  ],
  templateUrl: './validation-detail.component.html',
  styleUrl: './validation-detail.component.scss',
})
export class ValidationDetailComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);
  private readonly authService = inject(AuthService);

  // ── State ────────────────────────────────────────────────
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly decision = signal<DecisionResponse | null>(null);
  readonly submitting = signal(false);
  readonly submitError = signal<string | null>(null);
  readonly submitSuccess = signal(false);

  // ── Validation Form ──────────────────────────────────────
  readonly form = this.fb.group({
    decisionFinale: this.fb.nonNullable.control('', [Validators.required]),
    justificationHumaine: this.fb.nonNullable.control('', [
      Validators.required,
      Validators.minLength(10),
    ]),
    accordAvecIa: this.fb.nonNullable.control(true),
    confirmed: this.fb.nonNullable.control(false, [Validators.requiredTrue]),
  });

  // ── Computed ─────────────────────────────────────────────
  readonly domain = computed<DecisionDomain>(() =>
    resolveDomain(this.decision()?.domaine),
  );

  readonly domainMeta = computed(() => DOMAIN_META[this.domain()]);

  readonly decisionOptions = computed(() => this.domainMeta().humanDecisions);

  readonly isSelfValidation = computed(() => {
    const d = this.decision();
    const user = this.authService.currentUser;
    if (!d || !user) return false;
    return d.createdBy === user.email;
  });

  readonly isAlreadyValidated = computed(() => {
    const d = this.decision();
    if (!d) return false;
    const s = d.statutValidation;
    return s === 'VALIDEE' || s === 'APPROUVEE' || s === 'REJETEE';
  });

  readonly canValidate = computed(() =>
    !this.isSelfValidation() && !this.isAlreadyValidated() && !this.submitSuccess(),
  );

  readonly topFactors = computed<ExplanationFactor[]>(() => {
    const factors = this.decision()?.factors ?? [];
    return [...factors]
      .sort((a, b) => Math.abs(b.shapValue) - Math.abs(a.shapValue))
      .slice(0, 6);
  });

  readonly mlAgreement = computed(() => {
    const d = this.decision();
    const selected = this.form.get('decisionFinale')?.value;
    if (!d || !selected) return null;
    const mlDecision = d.suggestedDecision ?? d.mlPrediction?.decision ?? '';
    // Agreement: selected human decision aligns with ML suggestion
    const accepted = ['ACCEPTEE', 'APPROUVER', 'SUIVI_STANDARD', 'AUCUNE_INTERVENTION'];
    const rejected = ['REFUSEE', 'REJETER', 'ORIENTATION_SPECIALISTE', 'ORIENTATION', 'TUTORAT'];
    const mlPositive = accepted.some((v) => mlDecision.includes(v));
    const humanPositive = accepted.some((v) => selected.includes(v));
    const mlNegative = rejected.some((v) => mlDecision.includes(v));
    const humanNegative = rejected.some((v) => selected.includes(v));
    if ((mlPositive && humanPositive) || (mlNegative && humanNegative)) return true;
    if ((mlPositive && humanNegative) || (mlNegative && humanPositive)) return false;
    return null;
  });

  readonly featureEntries = computed<{ key: string; label: string; value: unknown }[]>(() => {
    const d = this.decision();
    if (!d?.features) return [];
    const domain = this.domain();
    return Object.entries(d.features).map(([key, value]) => ({
      key,
      label: featureDisplayName(domain, key),
      value,
    }));
  });

  constructor() {
    this.loadDecision();

    // Adjust min length depending on accord
    this.form.get('accordAvecIa')?.valueChanges.subscribe((agree) => {
      const ctrl = this.form.get('justificationHumaine');
      ctrl?.setValidators([Validators.required, Validators.minLength(agree ? 10 : 30)]);
      ctrl?.updateValueAndValidity();
    });

    // Auto-set accordAvecIa when decisionFinale changes
    this.form.get('decisionFinale')?.valueChanges.subscribe(() => {
      const agreement = this.mlAgreement();
      if (agreement !== null) {
        this.form.get('accordAvecIa')?.setValue(agreement, { emitEvent: false });
      }
    });
  }

  private loadDecision(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error.set('Identifiant de décision manquant.');
      this.loading.set(false);
      return;
    }
    this.loading.set(true);
    this.decisionService.getById(id).subscribe({
      next: (d) => {
        this.decision.set(d);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Impossible de charger ce dossier.'));
        this.loading.set(false);
      },
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const d = this.decision();
    if (!d) return;

    const { decisionFinale, justificationHumaine, accordAvecIa } = this.form.getRawValue();
    this.submitting.set(true);
    this.submitError.set(null);

    this.decisionService
      .validateDomain(d.decisionId, { decisionFinale, justificationHumaine, accordAvecIa })
      .subscribe({
        next: (updated) => {
          this.decision.set(updated);
          this.submitting.set(false);
          this.submitSuccess.set(true);
          // Return to queue after 1.5 s
          setTimeout(() => void this.router.navigate(['/validation']), 1500);
        },
        error: (err) => {
          this.submitError.set(resolveHttpErrorMessage(err, 'Erreur lors de la validation.'));
          this.submitting.set(false);
        },
      });
  }

  goBack(): void {
    void this.router.navigate(['/validation']);
  }

  hasError(field: string): boolean {
    const ctrl = this.form.get(field);
    return !!ctrl && ctrl.invalid && ctrl.touched;
  }

  // ── Display Helpers ──────────────────────────────────────
  decisionLabel = decisionLabel;
  riskLabel = riskLabel;

  domainLabel(d: string | undefined): string {
    if (!d) return 'Crédit';
    const map: Record<string, string> = {
      CREDIT: 'Crédit',
      MEDICAL: 'Médical',
      EDUCATION: 'Éducation',
    };
    return map[d] ?? d;
  }

  domainSeverity(d: string | undefined): 'info' | 'success' | 'warn' | 'secondary' {
    if (d === 'MEDICAL') return 'warn';
    if (d === 'EDUCATION') return 'success';
    return 'info';
  }

  mlSeverity(value: string | undefined): 'success' | 'danger' | 'warn' | 'secondary' {
    if (!value) return 'secondary';
    if (value.includes('FAIBLE') || value.includes('ACCEPT') || value.includes('APPROUV')
      || value.includes('SUIVI_STANDARD') || value.includes('AUCUNE')) return 'success';
    if (value.includes('ELEVE') || value.includes('REFUS') || value.includes('REJET')
      || value.includes('ORIENTATION_SPEC') || value.includes('DECROCHAGE')) return 'danger';
    return 'warn';
  }

  riskSeverity(risk: string | undefined): 'success' | 'warn' | 'danger' | 'secondary' {
    if (!risk) return 'secondary';
    const r = risk.toUpperCase();
    if (r.includes('FAIBLE') || r === 'LOW') return 'success';
    if (r.includes('MOYEN') || r === 'MEDIUM') return 'warn';
    if (r.includes('ELEVE') || r === 'HIGH') return 'danger';
    return 'secondary';
  }

  statutSeverity(s: string | undefined): 'success' | 'warn' | 'danger' | 'info' | 'secondary' {
    if (!s) return 'secondary';
    if (s === 'VALIDEE' || s === 'APPROUVEE') return 'success';
    if (s === 'REJETEE') return 'danger';
    if (s === 'EN_ATTENTE_VALIDATION' || s === 'EN_ATTENTE') return 'warn';
    if (s === 'A_REVOIR') return 'info';
    return 'secondary';
  }

  statutLabel(s: string | undefined): string {
    const map: Record<string, string> = {
      EN_ATTENTE_VALIDATION: 'En attente',
      EN_ATTENTE: 'En attente',
      VALIDEE: 'Validée',
      APPROUVEE: 'Approuvée',
      REJETEE: 'Rejetée',
      A_REVOIR: 'À revoir',
      ANALYSEE: 'Analysée',
      BROUILLON: 'Brouillon',
    };
    return map[s ?? ''] ?? (s ?? '—');
  }

  impactColor(factor: ExplanationFactor): string {
    if (factor.impact === 'POSITIVE') return 'var(--chip-approved-fg)';
    if (factor.impact === 'NEGATIVE') return 'var(--chip-rejected-fg)';
    return 'var(--muted)';
  }

  formatValue(v: unknown): string {
    if (v === null || v === undefined) return '—';
    if (typeof v === 'boolean') return v ? 'Oui' : 'Non';
    if (typeof v === 'number') return v.toLocaleString('fr-FR');
    return String(v);
  }
}
