import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { REGIONS_MAROC } from '../../../core/config/domains/domain.config';

@Component({
  selector: 'app-credit-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea],
  template: `
    <form [formGroup]="form" class="form-grid" (ngSubmit)="onSubmit()">
      <div class="field">
        <label for="secteurActivite">Secteur économique</label>
        <p-select inputId="secteurActivite" formControlName="secteurActivite"
          [options]="secteurs" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="region">Région</label>
        <p-select inputId="region" formControlName="region"
          [options]="regions" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="ageDemandeur">Âge du demandeur</label>
        <p-inputNumber inputId="ageDemandeur" formControlName="ageDemandeur" [min]="18" [max]="80" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="statutProfessionnel">Statut professionnel</label>
        <p-select inputId="statutProfessionnel" formControlName="statutProfessionnel"
          [options]="statuts" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="revenuMensuelMad">Revenu mensuel (MAD)</label>
        <p-inputNumber inputId="revenuMensuelMad" formControlName="revenuMensuelMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="chargesMensuellesMad">Charges mensuelles (MAD)</label>
        <p-inputNumber inputId="chargesMensuellesMad" formControlName="chargesMensuellesMad" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="montantDemandeMad">Montant demandé (MAD)</label>
        <p-inputNumber inputId="montantDemandeMad" formControlName="montantDemandeMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="dureeCreditMois">Durée (mois)</label>
        <p-inputNumber inputId="dureeCreditMois" formControlName="dureeCreditMois" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="ancienneteProfessionnelleAnnees">Ancienneté professionnelle (années)</label>
        <p-inputNumber inputId="ancienneteProfessionnelleAnnees" formControlName="ancienneteProfessionnelleAnnees" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="creditsExistants">Crédits existants</label>
        <p-inputNumber inputId="creditsExistants" formControlName="creditsExistants" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="incidentsPaiement24Mois">Incidents de paiement (24 mois)</label>
        <p-inputNumber inputId="incidentsPaiement24Mois" formControlName="incidentsPaiement24Mois" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="ratioEndettement">Ratio d'endettement (0–1)</label>
        <p-inputNumber inputId="ratioEndettement" formControlName="ratioEndettement"
          [min]="0" [max]="1" [minFractionDigits]="2" [maxFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="typeGarantie">Type de garantie</label>
        <p-select inputId="typeGarantie" formControlName="typeGarantie"
          [options]="garanties" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="typeCredit">Type de crédit</label>
        <p-select inputId="typeCredit" formControlName="typeCredit"
          [options]="typesCredit" optionLabel="label" optionValue="value" styleClass="w-full" />
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

  readonly regions = [...REGIONS_MAROC];
  readonly secteurs = [
    { label: 'Services', value: 'SERVICES' },
    { label: 'Industrie', value: 'INDUSTRIE' },
    { label: 'Commerce', value: 'COMMERCE' },
    { label: 'Tech', value: 'TECH' },
    { label: 'Agriculture', value: 'AGRICULTURE' },
  ];
  readonly statuts = [
    { label: 'Salarié CDI', value: 'SALARIE_CDI' },
    { label: 'Salarié CDD', value: 'SALARIE_CDD' },
    { label: 'Fonctionnaire', value: 'FONCTIONNAIRE' },
    { label: 'Indépendant', value: 'INDEPENDANT' },
    { label: 'Retraité', value: 'RETRAITE' },
  ];
  readonly garanties = [
    { label: 'Aucune', value: 'AUCUNE' },
    { label: 'Hypothèque', value: 'HYPOTHEQUE' },
    { label: 'Caution', value: 'CAUTION' },
    { label: 'Nantissement', value: 'NANTISSEMENT' },
  ];
  readonly typesCredit = [
    { label: 'Consommation', value: 'CONSOMMATION' },
    { label: 'Immobilier', value: 'IMMOBILIER' },
    { label: 'Professionnel', value: 'PROFESSIONNEL' },
    { label: 'Auto', value: 'AUTO' },
  ];

  readonly form = this.fb.group({
    secteurActivite: ['SERVICES', Validators.required],
    region: ['Casablanca-Settat', Validators.required],
    ageDemandeur: [35, [Validators.required, Validators.min(18), Validators.max(80)]],
    statutProfessionnel: ['SALARIE_CDI', Validators.required],
    revenuMensuelMad: [12000, [Validators.required, Validators.min(1)]],
    chargesMensuellesMad: [4000, [Validators.required, Validators.min(0)]],
    montantDemandeMad: [80000, [Validators.required, Validators.min(1)]],
    dureeCreditMois: [48, [Validators.required, Validators.min(1)]],
    ancienneteProfessionnelleAnnees: [8, [Validators.required, Validators.min(0)]],
    creditsExistants: [1, [Validators.required, Validators.min(0)]],
    incidentsPaiement24Mois: [0, [Validators.required, Validators.min(0)]],
    ratioEndettement: [0.33, [Validators.required, Validators.min(0), Validators.max(1)]],
    typeGarantie: ['CAUTION', Validators.required],
    typeCredit: ['CONSOMMATION', Validators.required],
    description: ['Demande de crédit — données synthétiques de démonstration'],
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
