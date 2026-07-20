import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Card } from 'primeng/card';
import { Textarea } from 'primeng/textarea';
import { InputNumber } from 'primeng/inputnumber';
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
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { DecisionResponse } from '../../../core/models/decision.models';
import {
  buildAnalyzePayload,
  ML_FEATURE_KEYS,
  ML_SCHEMA_INFO,
  Sector,
  SectorFieldConfig,
  sectorFields,
  SECTORS,
  validatorsForField,
} from '../../../core/config/sector-fields.config';

@Component({
  selector: 'app-decision-new',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    Card,
    Textarea,
    InputNumber,
    Select,
    Button,
    Message,
    ProgressSpinner,
    Divider,
    Checkbox,
    Tag,
    ConsensusCardComponent,
    AgentResponseCardComponent,
  ],
  templateUrl: './decision-new.component.html',
  styleUrl: './decision-new.component.scss',
})
export class DecisionNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly sectors = SECTORS.map((sector) => ({ label: sector, value: sector }));
  readonly schemaInfo = ML_SCHEMA_INFO;
  readonly multiAgentLabels = MULTI_AGENT_UI_LABELS;
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<DecisionResponse | null>(null);
  readonly activeFields = signal<SectorFieldConfig[]>(sectorFields.SERVICES);
  readonly showValidationSummary = signal(false);
  readonly validationErrors = signal<string[]>([]);

  readonly form: FormGroup = this.fb.group({
    sector: ['SERVICES' as Sector, Validators.required],
    description: ['Demande de crédit professionnelle'],
    includeOpenRouter: [true],
  });

  ngOnInit(): void {
    this.rebuildMlControls(this.form.get('sector')!.value as Sector);
    this.form
      .get('sector')!
      .valueChanges.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sector) => this.rebuildMlControls(sector as Sector));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.showValidationSummary.set(true);
      this.validationErrors.set(this.collectValidationErrors());
      return;
    }

    this.showValidationSummary.set(false);
    this.validationErrors.set([]);
    this.loading.set(true);
    this.error.set(null);
    this.result.set(null);

    const sector = this.form.get('sector')!.value as Sector;
    const payload = buildAnalyzePayload(this.form.getRawValue(), sector);

    this.decisionService.analyze(payload).subscribe({
      next: (response) => {
        this.result.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(resolveHttpErrorMessage(err, 'Erreur lors de l\'analyse de la décision.'));
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

  goBack(): void {
    void this.router.navigate(['/decisions']);
  }

  mlSeverity(decision: string | undefined): 'success' | 'danger' | 'secondary' {
    if (decision === 'APPROUVER') return 'success';
    if (decision === 'REJETER') return 'danger';
    return 'secondary';
  }

  riskSeverity(risk: string | undefined): 'success' | 'warn' | 'danger' | 'secondary' {
    if (risk === 'LOW') return 'success';
    if (risk === 'MEDIUM') return 'warn';
    if (risk === 'HIGH') return 'danger';
    return 'secondary';
  }

  private rebuildMlControls(sector: Sector): void {
    for (const key of ML_FEATURE_KEYS) {
      if (this.form.contains(key)) {
        this.form.removeControl(key);
      }
    }

    const fields = sectorFields[sector];
    for (const field of fields) {
      this.form.addControl(
        field.key,
        this.fb.control(field.defaultValue, validatorsForField(field)),
      );
    }

    this.activeFields.set(fields);
  }

  private collectValidationErrors(): string[] {
    const messages: string[] = [];
    for (const [key, control] of Object.entries(this.form.controls)) {
      if (!control.invalid) continue;
      if (key === 'sector') {
        messages.push('Le secteur est obligatoire.');
        continue;
      }
      if (key === 'description') {
        messages.push('La description est invalide.');
        continue;
      }
      const field = this.activeFields().find((f) => f.key === key);
      messages.push(`${field?.label ?? key} est invalide ou hors bornes.`);
    }
    return messages;
  }
}
