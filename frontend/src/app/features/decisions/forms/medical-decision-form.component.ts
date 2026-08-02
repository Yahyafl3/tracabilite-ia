import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { DOMAIN_META } from '../../../core/config/domains/domain.config';

@Component({
  selector: 'app-medical-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Textarea, Message],
  template: `
    <p-message severity="warn" [text]="disclaimer" styleClass="mb-3 w-full" />
    <form [formGroup]="form" class="form-grid">
      <div class="field">
        <label for="age">Âge</label>
        <p-inputNumber inputId="age" formControlName="age" [min]="1" [max]="120" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="grossesses">Grossesses</label>
        <p-inputNumber inputId="grossesses" formControlName="grossesses" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="glycemieMgDl">Glycémie (mg/dL)</label>
        <p-inputNumber inputId="glycemieMgDl" formControlName="glycemieMgDl"
          [min]="0" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="pressionArterielleMmhg">Pression artérielle (mmHg)</label>
        <p-inputNumber inputId="pressionArterielleMmhg" formControlName="pressionArterielleMmhg"
          [min]="0" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="epaisseurPliCutaneMm">Épaisseur pli cutané (mm)</label>
        <p-inputNumber inputId="epaisseurPliCutaneMm" formControlName="epaisseurPliCutaneMm"
          [min]="0" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="insulineMicroUMl">Insuline (µU/mL)</label>
        <p-inputNumber inputId="insulineMicroUMl" formControlName="insulineMicroUMl"
          [min]="0" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="imcKgM2">IMC (kg/m²)</label>
        <p-inputNumber inputId="imcKgM2" formControlName="imcKgM2"
          [min]="10" [max]="80" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field field--full">
        <label for="description">Description</label>
        <textarea pTextarea id="description" formControlName="description" rows="3" class="w-full"></textarea>
      </div>
    </form>
  `,
  styles: [
    `
      .form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 1rem;
      }
      .field--full {
        grid-column: 1 / -1;
      }
      label {
        display: block;
        font-weight: 600;
        margin-bottom: 0.35rem;
      }
      @media (max-width: 700px) {
        .form-grid {
          grid-template-columns: 1fr;
        }
      }
    `,
  ],
})
export class MedicalDecisionFormComponent {
  @Output() readonly formReady = new EventEmitter<{ valid: boolean; value: Record<string, unknown> }>();

  private readonly fb = inject(FormBuilder);
  readonly disclaimer = DOMAIN_META.MEDICAL.disclaimer!;

  readonly form = this.fb.group({
    age: [45, [Validators.required, Validators.min(1), Validators.max(120)]],
    grossesses: [0, [Validators.required, Validators.min(0)]],
    glycemieMgDl: [120, [Validators.required, Validators.min(0)]],
    pressionArterielleMmhg: [72, [Validators.required, Validators.min(0)]],
    epaisseurPliCutaneMm: [25, [Validators.required, Validators.min(0)]],
    insulineMicroUMl: [85, [Validators.required, Validators.min(0)]],
    imcKgM2: [28.5, [Validators.required, Validators.min(10), Validators.max(80)]],
    description: ['Évaluation indicative — données de démonstration'],
  });

  constructor() {
    this.form.statusChanges.subscribe(() => this.emit());
    this.form.valueChanges.subscribe(() => this.emit());
    this.emit();
  }

  get rawValue(): Record<string, unknown> {
    return this.form.getRawValue() as Record<string, unknown>;
  }

  get valid(): boolean {
    return this.form.valid;
  }

  private emit(): void {
    this.formReady.emit({ valid: this.form.valid, value: this.rawValue });
  }
}
