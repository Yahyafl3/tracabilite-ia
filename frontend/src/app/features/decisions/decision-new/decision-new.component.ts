import { Component, computed, inject, OnInit, signal, viewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { Select } from 'primeng/select';
import { Button } from 'primeng/button';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Divider } from 'primeng/divider';
import { Checkbox } from 'primeng/checkbox';
import { Tag } from 'primeng/tag';
import { TranslatePipe } from '@ngx-translate/core';
import {
  AgentResponseCardComponent,
  ConsensusCardComponent,
} from '../../../shared/ui';
import { DecisionService } from '../../../core/services/decision.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models/auth.models';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { decisionLabel, domainLabel } from '../../../core/utils/label.util';
import { DecisionResponse } from '../../../core/models/decision.models';
import {
  DECISION_DOMAINS,
  DecisionDomain,
  DOMAIN_META,
} from '../../../core/config/domains/domain.config';
import { CreditDecisionFormComponent } from '../forms/credit-decision-form.component';
import { MedicalDecisionFormComponent } from '../forms/medical-decision-form.component';
import { EducationDecisionFormComponent } from '../forms/education-decision-form.component';
import { TranslationService } from '../../../core/i18n/translation.service';

/** Map role → unique allowed domain (null = all domains). */
const ROLE_DOMAIN_MAP: Partial<Record<string, DecisionDomain>> = {
  [UserRole.AGENT_CREDIT]:      'CREDIT',
  [UserRole.AGENT_SANTE]:       'MEDICAL',
  [UserRole.AGENT_PEDAGOGIQUE]: 'EDUCATION',
};

@Component({
  selector: 'app-decision-new',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    Select,
    Button,
    Message,
    ProgressSpinner,
    Divider,
    Checkbox,
    Tag,
    ConsensusCardComponent,
    AgentResponseCardComponent,
    CreditDecisionFormComponent,
    MedicalDecisionFormComponent,
    EducationDecisionFormComponent,
    TranslatePipe,
  ],
  templateUrl: './decision-new.component.html',
  styleUrl: './decision-new.component.scss',
})
export class DecisionNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly decisions = inject(DecisionService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);
  private readonly i18n = inject(TranslationService);

  readonly multiAgentLabels = computed(() => {
    this.i18n.currentLang();
    return {
      consensus: this.i18n.t('shared.consensus'),
      agentResponses: this.i18n.t('shared.agentResponses'),
      synthesis: this.i18n.t('shared.synthesis'),
    };
  });
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<DecisionResponse | null>(null);
  readonly childValid = signal(false);
  readonly childValue = signal<Record<string, unknown>>({});

  readonly creditForm = viewChild(CreditDecisionFormComponent);
  readonly medicalForm = viewChild(MedicalDecisionFormComponent);
  readonly educationForm = viewChild(EducationDecisionFormComponent);

  /** Domains available to the current user based on role. */
  get allowedDomains(): { value: DecisionDomain; label: string; description: string }[] {
    this.i18n.currentLang();
    const role = this.authService.currentUser?.role as string | undefined;
    const locked = role ? ROLE_DOMAIN_MAP[role] : undefined;
    const source = locked ? DECISION_DOMAINS.filter((d) => d.value === locked) : DECISION_DOMAINS;
    return source.map((d) => ({
      ...d,
      label: domainLabel(d.value),
      description: this.i18n.t(`domainMeta.${d.value}.description`),
    }));
  }

  /** True when the user is restricted to exactly one domain. */
  get domainLocked(): boolean {
    const role = this.authService.currentUser?.role as string | undefined;
    return role ? !!ROLE_DOMAIN_MAP[role] : false;
  }

  readonly shellForm: FormGroup = this.fb.group({
    domaine: ['CREDIT' as DecisionDomain, Validators.required],
    includeOpenRouter: [true],
  });

  ngOnInit(): void {
    const domains = this.allowedDomains;
    if (domains.length === 1) {
      this.shellForm.patchValue({ domaine: domains[0].value });
    }
  }

  get selectedDomain(): DecisionDomain {
    return this.shellForm.get('domaine')!.value as DecisionDomain;
  }

  get domainMeta() {
    return DOMAIN_META[this.selectedDomain];
  }

  get domainDescription(): string {
    this.i18n.currentLang();
    return this.i18n.t(`domainMeta.${this.selectedDomain}.description`);
  }

  domainName(domain: string = this.selectedDomain): string {
    this.i18n.currentLang();
    return domainLabel(domain);
  }

  metaText(field: 'useCase' | 'riskLabel'): string {
    this.i18n.currentLang();
    return this.i18n.t(`domainMeta.${this.selectedDomain}.${field}`);
  }

  validatorRoleLabel(): string {
    this.i18n.currentLang();
    return this.i18n.t(`roles.${this.domainMeta.validatorRole}`);
  }

  mlLabel(value?: string | null): string {
    this.i18n.currentLang();
    return decisionLabel(value);
  }

  riskCodeLabel(value?: string | null): string {
    this.i18n.currentLang();
    return value ? this.i18n.t(`riskCode.${value}`) : this.i18n.t('common.dash');
  }

  onChildForm(event: { valid: boolean; value: Record<string, unknown> }): void {
    this.childValid.set(event.valid);
    this.childValue.set(event.value);
  }

  onDomainChange(): void {
    this.result.set(null);
    this.error.set(null);
    this.childValid.set(false);
    this.childValue.set({});
  }

  submit(): void {
    if (!this.childValid()) {
      this.error.set(this.i18n.t('decisions.new.formInvalid'));
      return;
    }

    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    const payload = {
      ...this.childValue(),
      includeAgents: !!this.shellForm.get('includeOpenRouter')?.value,
    };

    const domain = this.selectedDomain;
    const request$ =
      domain === 'CREDIT'
        ? this.decisions.createCredit(payload)
        : domain === 'MEDICAL'
          ? this.decisions.createMedical(payload)
          : this.decisions.createEducation(payload);

    request$.subscribe({
      next: (response) => {
        this.result.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.new.analyzeError')));
        this.loading.set(false);
      },
    });
  }

  openDetail(): void {
    const id = this.result()?.decisionId;
    if (id) {
      void this.router.navigate(['/decisions', id]);
    }
  }

  submitForValidation(): void {
    const id = this.result()?.decisionId;
    if (!id) return;
    this.loading.set(true);
    this.decisions.submitForValidation(id).subscribe({
      next: (response) => {
        this.result.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, this.i18n.t('decisions.new.submitError')));
        this.loading.set(false);
      },
    });
  }

  goBack(): void {
    void this.router.navigate(['/decisions']);
  }

  mlSeverity(decision: string | undefined): 'success' | 'danger' | 'warn' | 'secondary' {
    if (!decision) return 'secondary';
    if (decision.includes('FAIBLE')) return 'success';
    if (decision.includes('MOYEN') || decision.includes('MODERE')) return 'warn';
    if (decision.includes('ELEVE') || decision === 'REJETER') return 'danger';
    if (decision === 'APPROUVER') return 'success';
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
}
