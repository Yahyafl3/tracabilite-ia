import { Component, EventEmitter, Output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-credit-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea, TranslatePipe],
  template: `
    <form [formGroup]="form" class="form-grid" (ngSubmit)="onSubmit()">
      <div class="field">
        <label for="age">{{ 'forms.age' | translate }}</label>
        <p-inputNumber inputId="age" formControlName="age" [min]="18" [max]="100" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="dureeMois">{{ 'forms.credit.duration' | translate }}</label>
        <p-inputNumber inputId="dureeMois" formControlName="dureeMois" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="typeContrat">{{ 'forms.credit.contractType' | translate }}</label>
        <p-select inputId="typeContrat" formControlName="typeContrat"
          [options]="typesContrat()" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="statutLogement">{{ 'forms.credit.housing' | translate }}</label>
        <p-select inputId="statutLogement" formControlName="statutLogement"
          [options]="statutsLogement()" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="incidentPaiementBam">{{ 'forms.credit.bamIncidents' | translate }}</label>
        <p-inputNumber inputId="incidentPaiementBam" formControlName="incidentPaiementBam" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="montantDemandeMad">{{ 'forms.credit.amount' | translate }}</label>
        <p-inputNumber inputId="montantDemandeMad" formControlName="montantDemandeMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="nouvelleEcheanceMad">{{ 'forms.credit.installment' | translate }}</label>
        <p-inputNumber inputId="nouvelleEcheanceMad" formControlName="nouvelleEcheanceMad" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="revenuMensuelMad">{{ 'forms.credit.income' | translate }}</label>
        <p-inputNumber inputId="revenuMensuelMad" formControlName="revenuMensuelMad" [min]="1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="tauxEndettement">{{ 'forms.credit.debtRatio' | translate }}</label>
        <p-inputNumber inputId="tauxEndettement" formControlName="tauxEndettement"
          [min]="0" [max]="1" [minFractionDigits]="2" [maxFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field field--full">
        <label for="description">{{ 'forms.description' | translate }}</label>
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
  private readonly i18n = inject(TranslationService);

  readonly typesContrat = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('forms.credit.cdi'), value: 'CDI' },
      { label: this.i18n.t('forms.credit.cdd'), value: 'CDD' },
      { label: this.i18n.t('forms.credit.civilServant'), value: 'FONCTIONNAIRE' },
      { label: this.i18n.t('forms.credit.informal'), value: 'INFORMEL' },
    ];
  });
  readonly statutsLogement = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('forms.credit.owner'), value: 'PROPRIETAIRE' },
      { label: this.i18n.t('forms.credit.tenant'), value: 'LOCATAIRE' },
      { label: this.i18n.t('forms.credit.companyHousing'), value: 'LOGEMENT_DE_FONCTION' },
    ];
  });

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
