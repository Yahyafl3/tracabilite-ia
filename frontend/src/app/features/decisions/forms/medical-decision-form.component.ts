import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { DOMAIN_META, REGIONS_MAROC } from '../../../core/config/domains/domain.config';

@Component({
  selector: 'app-medical-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea, Message],
  template: `
    <p-message severity="warn" [text]="disclaimer" styleClass="mb-3 w-full" />
    <form [formGroup]="form" class="form-grid">
      <div class="field">
        <label for="region">Région</label>
        <p-select inputId="region" formControlName="region" [options]="regions" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="age">Âge</label>
        <p-inputNumber inputId="age" formControlName="age" [min]="1" [max]="120" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="sexe">Sexe</label>
        <p-select inputId="sexe" formControlName="sexe" [options]="sexes" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="imc">IMC</label>
        <p-inputNumber inputId="imc" formControlName="imc" [min]="10" [max]="60" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="niveauActivitePhysique">Activité physique</label>
        <p-select inputId="niveauActivitePhysique" formControlName="niveauActivitePhysique"
          [options]="activites" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="glycemie">Glycémie</label>
        <p-inputNumber inputId="glycemie" formControlName="glycemie" [min]="0.1" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      @for (f of boolFields; track f.key) {
        <div class="field">
          <label [for]="f.key">{{ f.label }}</label>
          <p-select [inputId]="f.key" [formControlName]="f.key" [options]="ouiNon" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
      }
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
  readonly regions = [...REGIONS_MAROC];
  readonly sexes = [
    { label: 'Homme', value: 'HOMME' },
    { label: 'Femme', value: 'FEMME' },
  ];
  readonly activites = [
    { label: 'Sédentaire', value: 'SEDENTAIRE' },
    { label: 'Léger', value: 'LEGER' },
    { label: 'Modéré', value: 'MODERE' },
    { label: 'Intense', value: 'INTENSE' },
  ];
  readonly ouiNon = [
    { label: 'Oui', value: 'OUI' },
    { label: 'Non', value: 'NON' },
  ];
  readonly boolFields = [
    { key: 'antecedentsFamiliauxDiabete', label: 'Antécédents familiaux diabète' },
    { key: 'hypertension', label: 'Hypertension' },
    { key: 'polyurie', label: 'Polyurie' },
    { key: 'polydipsie', label: 'Polydipsie' },
    { key: 'pertePoidsSoudaine', label: 'Perte de poids soudaine' },
    { key: 'faiblesse', label: 'Faiblesse' },
    { key: 'obesite', label: 'Obésité' },
    { key: 'suiviMedical', label: 'Suivi médical' },
  ] as const;

  readonly form = this.fb.group({
    region: ['Rabat-Salé-Kénitra', Validators.required],
    age: [45, [Validators.required, Validators.min(1), Validators.max(120)]],
    sexe: ['HOMME', Validators.required],
    imc: [28.5, [Validators.required, Validators.min(10), Validators.max(60)]],
    niveauActivitePhysique: ['LEGER', Validators.required],
    antecedentsFamiliauxDiabete: ['NON', Validators.required],
    hypertension: ['NON', Validators.required],
    glycemie: [1.1, [Validators.required, Validators.min(0.1)]],
    polyurie: ['NON', Validators.required],
    polydipsie: ['NON', Validators.required],
    pertePoidsSoudaine: ['NON', Validators.required],
    faiblesse: ['NON', Validators.required],
    obesite: ['NON', Validators.required],
    suiviMedical: ['OUI', Validators.required],
    description: ['Évaluation indicative — données synthétiques'],
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
