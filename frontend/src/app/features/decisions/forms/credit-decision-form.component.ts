import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';

@Component({
  selector: 'app-credit-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea],
  template: `
    <form [formGroup]="form" class="form-grid" (ngSubmit)="onSubmit()">
      <div class="field">
        <label for="age">Âge</label>
        <p-inputNumber inputId="age" formControlName="age" [min]="18" [max]="100" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="dureeMois">Durée (mois)</label>
        <p-inputNumber inputId="dureeMois" formControlName="dureeMois" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="typeContrat">Type de contrat</label>
        <p-select inputId="typeContrat" formControlName="typeContrat"
          [options]="typesContrat" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="statutLogement">Statut logement</label>
        <p-select inputId="statutLogement" formControlName="statutLogement"
          [options]="statutsLogement" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="incidentPaiementBam">Incidents paiement BAM</label>
        <p-inputNumber inputId="incidentPaiementBam" formControlName="incidentPaiementBam" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="montantDemandeMad">Montant demandé (MAD)</label>
        <p-inputNumber inputId="montantDemandeMad" formControlName="montantDemandeMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="nouvelleEcheanceMad">Nouvelle échéance (MAD)</label>
        <p-inputNumber inputId="nouvelleEcheanceMad" formControlName="nouvelleEcheanceMad" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="revenuMensuelMad">Revenu mensuel (MAD)</label>
        <p-inputNumber inputId="revenuMensuelMad" formControlName="revenuMensuelMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="tauxEndettement">Taux d'endettement (0–1)</label>
        <p-inputNumber inputId="tauxEndettement" formControlName="tauxEndettement"
          [min]="0" [max]="1" [minFractionDigits]="2" [maxFractionDigits]="2" [step]="0.01" styleClass="w-full" />
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
export class CreditDecisionFormComponent {
  @Output() readonly formReady = new EventEmitter<{ valid: boolean; value: Record<string, unknown> }>();

  private readonly fb = inject(FormBuilder);

  readonly typesContrat = [
    { label: 'CDI', value: 'CDI' },
    { label: 'CDD', value: 'CDD' },
    { label: 'Fonctionnaire', value: 'FONCTIONNAIRE' },
    { label: 'Informel', value: 'INFORMEL' },
  ];
  readonly statutsLogement = [
    { label: 'Propriétaire', value: 'PROPRIETAIRE' },
    { label: 'Locataire', value: 'LOCATAIRE' },
    { label: 'Logement de fonction', value: 'LOGEMENT_DE_FONCTION' },
  ];

  readonly form = this.fb.group({
    age: [35, [Validators.required, Validators.min(18), Validators.max(100)]],
    dureeMois: [48, [Validators.required, Validators.min(1)]],
    typeContrat: ['CDI', Validators.required],
    statutLogement: ['PROPRIETAIRE', Validators.required],
    incidentPaiementBam: [0, [Validators.required, Validators.min(0)]],
    montantDemandeMad: [80000, [Validators.required, Validators.min(1)]],
    nouvelleEcheanceMad: [2500, [Validators.required, Validators.min(0)]],
    revenuMensuelMad: [12000, [Validators.required, Validators.min(1)]],
    tauxEndettement: [0.33, [Validators.required, Validators.min(0), Validators.max(1)]],
    description: ['Demande de crédit — données de démonstration'],
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

  onSubmit(): void {
    this.form.markAllAsTouched();
    this.emit();
  }

  private emit(): void {
    this.formReady.emit({ valid: this.form.valid, value: this.rawValue });
  }
}
