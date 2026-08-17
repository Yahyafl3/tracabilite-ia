import { Component, EventEmitter, Output, computed, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { TranslatePipe } from '@ngx-translate/core';
import { TranslationService } from '../../../core/i18n/translation.service';

@Component({
  selector: 'app-education-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea, Message, TranslatePipe],
  template: `
    <p-message severity="info" [text]="disclaimer()" styleClass="mb-3 w-full" />
    <form [formGroup]="form" class="form-grid">
      <div class="field">
        <label for="ageInscription">{{ 'forms.education.ageAtEnrollment' | translate }}</label>
        <p-inputNumber inputId="ageInscription" formControlName="ageInscription"
          [min]="15" [max]="80" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="noteAdmission">{{ 'forms.education.admissionGrade' | translate }}</label>
        <p-inputNumber inputId="noteAdmission" formControlName="noteAdmission"
          [min]="0" [max]="200" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="noteQualificationPrecedente">{{ 'forms.education.previousQualification' | translate }}</label>
        <p-inputNumber inputId="noteQualificationPrecedente" formControlName="noteQualificationPrecedente"
          [min]="0" [max]="200" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="unitesValideesS1">{{ 'forms.education.unitsS1' | translate }}</label>
        <p-inputNumber inputId="unitesValideesS1" formControlName="unitesValideesS1" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="moyenneS1">{{ 'forms.education.avgS1' | translate }}</label>
        <p-inputNumber inputId="moyenneS1" formControlName="moyenneS1"
          [min]="0" [max]="20" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="unitesValideesS2">{{ 'forms.education.unitsS2' | translate }}</label>
        <p-inputNumber inputId="unitesValideesS2" formControlName="unitesValideesS2" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="moyenneS2">{{ 'forms.education.avgS2' | translate }}</label>
        <p-inputNumber inputId="moyenneS2" formControlName="moyenneS2"
          [min]="0" [max]="20" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="tauxChomage">{{ 'forms.education.unemployment' | translate }}</label>
        <p-inputNumber inputId="tauxChomage" formControlName="tauxChomage"
          [min]="-50" [max]="50" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="tauxInflation">{{ 'forms.education.inflation' | translate }}</label>
        <p-inputNumber inputId="tauxInflation" formControlName="tauxInflation"
          [min]="-50" [max]="50" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="pib">{{ 'forms.education.gdp' | translate }}</label>
        <p-inputNumber inputId="pib" formControlName="pib"
          [min]="-50" [max]="50" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="sexe">{{ 'forms.education.sex' | translate }}</label>
        <p-select inputId="sexe" formControlName="sexe"
          [options]="sexes()" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      @for (f of boolFields(); track f.key) {
        <div class="field">
          <label [for]="f.key">{{ f.label }}</label>
          <p-select [inputId]="f.key" [formControlName]="f.key"
            [options]="ouiNon()" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
      }
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
export class EducationDecisionFormComponent {
  @Output() readonly formReady = new EventEmitter<{ valid: boolean; value: Record<string, unknown> }>();

  private readonly fb = inject(FormBuilder);
  private readonly i18n = inject(TranslationService);

  readonly disclaimer = computed(() => {
    this.i18n.currentLang();
    return this.i18n.t('domainMeta.EDUCATION.disclaimer');
  });

  readonly sexes = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('forms.education.male'), value: 'HOMME' },
      { label: this.i18n.t('forms.education.female'), value: 'FEMME' },
    ];
  });
  readonly ouiNon = computed(() => {
    this.i18n.currentLang();
    return [
      { label: this.i18n.t('forms.yesNo.yes'), value: 'OUI' },
      { label: this.i18n.t('forms.yesNo.no'), value: 'NON' },
    ];
  });
  readonly boolFields = computed(() => {
    this.i18n.currentLang();
    return [
      { key: 'boursier', label: this.i18n.t('forms.education.scholarship') },
      { key: 'fraisAJour', label: this.i18n.t('forms.education.feesUpToDate') },
      { key: 'debiteur', label: this.i18n.t('forms.education.debtor') },
      { key: 'deplace', label: this.i18n.t('forms.education.displaced') },
      { key: 'international', label: this.i18n.t('forms.education.international') },
    ] as const;
  });

  readonly form = this.fb.group({
    ageInscription: [18, [Validators.required, Validators.min(15), Validators.max(80)]],
    noteAdmission: [120, [Validators.required, Validators.min(0), Validators.max(200)]],
    noteQualificationPrecedente: [110, [Validators.required, Validators.min(0), Validators.max(200)]],
    unitesValideesS1: [5, [Validators.required, Validators.min(0)]],
    moyenneS1: [11.5, [Validators.required, Validators.min(0), Validators.max(20)]],
    unitesValideesS2: [4, [Validators.required, Validators.min(0)]],
    moyenneS2: [11.0, [Validators.required, Validators.min(0), Validators.max(20)]],
    tauxChomage: [10.5, [Validators.required, Validators.min(-50), Validators.max(50)]],
    tauxInflation: [2.1, [Validators.required, Validators.min(-50), Validators.max(50)]],
    pib: [1.8, [Validators.required, Validators.min(-50), Validators.max(50)]],
    sexe: ['HOMME', Validators.required],
    boursier: ['NON', Validators.required],
    fraisAJour: ['OUI', Validators.required],
    debiteur: ['NON', Validators.required],
    deplace: ['NON', Validators.required],
    international: ['NON', Validators.required],
    description: ['Évaluation pédagogique — données de démonstration'],
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
