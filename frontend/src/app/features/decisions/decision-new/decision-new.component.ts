import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { IconComponent } from '../../../shared/icon.component';
import { DecisionService } from '../../../core/services/decision.service';
import { resolveHttpErrorMessage } from '../../../core/utils/http-error.util';
import { decisionChipClass, riskChipClass } from '../../../core/utils/chip-class.util';
import { riskLabel } from '../../../core/utils/label.util';
import { DecisionResponse } from '../../../core/models/decision.models';
import {
  ConsensusResponse,
  formatConsensusDisplay,
  type ConsensusDisplay,
} from '../../../core/models/openrouter.models';
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
  imports: [CommonModule, ReactiveFormsModule, RouterModule, IconComponent],
  templateUrl: './decision-new.component.html',
  styleUrl: './decision-new.component.scss',
})
export class DecisionNewComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly decisionService = inject(DecisionService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly sectors = SECTORS;
  readonly schemaInfo = ML_SCHEMA_INFO;
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly result = signal<DecisionResponse | null>(null);
  readonly activeFields = signal<SectorFieldConfig[]>(sectorFields.SERVICES);

  readonly form: FormGroup = this.fb.group({
    sector: ['SERVICES' as Sector, Validators.required],
    description: ['Demande de crédit professionnelle'],
    includeOpenRouter: [true],
  });

  ngOnInit(): void {
    this.rebuildMlControls(this.form.get('sector')!.value as Sector);
    this.form.get('sector')!.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((sector) => this.rebuildMlControls(sector as Sector));
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

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

  decisionChipClass = decisionChipClass;
  riskChipClass = riskChipClass;
  riskLabel = riskLabel;

  consensusDisplay(consensus: ConsensusResponse): ConsensusDisplay {
    return formatConsensusDisplay(consensus);
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
}
