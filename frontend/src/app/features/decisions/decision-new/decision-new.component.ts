import { Component, inject, OnInit, signal, viewChild } from '@angular/core';
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
import {
  AgentResponseCardComponent,
  ConsensusCardComponent,
} from '../../../shared/ui';
import { MULTI_AGENT_UI_LABELS } from '../../../shared/ui/multi-agent-ui.labels';
import { DecisionService } from '../../../core/services/decision.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserRole } from '../../../core/models/auth.models';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { DecisionResponse } from '../../../core/models/decision.models';
import {
  DECISION_DOMAINS,
  DecisionDomain,
  DOMAIN_META,
} from '../../../core/config/domains/domain.config';
import { CreditDecisionFormComponent } from '../forms/credit-decision-form.component';
import { MedicalDecisionFormComponent } from '../forms/medical-decision-form.component';
import { EducationDecisionFormComponent } from '../forms/education-decision-form.component';

/** Map role → unique allowed domain (null = all domains). */
const ROLE_DOMAIN_MAP: Partial<Record<string, DecisionDomain>> = {
  [UserRole.AGENT_CREDIT]:      'CREDIT',
  [UserRole.AGENT_SANTE]:       'MEDICAL',
  [UserRole.AGENT_PEDAGOGIQUE]: 'EDUCATION',
  // Legacy
  [UserRole.UTILISATEUR]:       'CREDIT',
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
  ],
  templateUrl: './decision-new.component.html',
  styleUrl: './decision-new.component.scss',
})
export class DecisionNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly decisions = inject(DecisionService);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly multiAgentLabels = MULTI_AGENT_UI_LABELS;
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
    const role = this.authService.currentUser?.role as string | undefined;
    const locked = role ? ROLE_DOMAIN_MAP[role] : undefined;
    if (locked) {
      return DECISION_DOMAINS.filter((d) => d.value === locked);
    }
    return DECISION_DOMAINS;
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
    return DECISION_DOMAINS.find((d) => d.value === this.selectedDomain)?.description ?? '';
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
      this.error.set('Veuillez corriger les champs du formulaire avant de lancer l’analyse.');
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
        this.error.set(resolveHttpErrorMessage(err, 'Erreur lors de l’analyse de la décision.'));
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
        this.error.set(resolveHttpErrorMessage(err, 'Soumission à validation impossible.'));
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
