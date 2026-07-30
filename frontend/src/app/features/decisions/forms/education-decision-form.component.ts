import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InputNumber } from 'primeng/inputnumber';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { Message } from 'primeng/message';
import { DOMAIN_META, REGIONS_MAROC } from '../../../core/config/domains/domain.config';

@Component({
  selector: 'app-education-decision-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, InputNumber, Select, Textarea, Message],
  template: `
    <p-message severity="info" [text]="disclaimer" styleClass="mb-3 w-full" />
    <form [formGroup]="form" class="form-grid">
      <div class="field">
        <label for="region">Région</label>
        <p-select inputId="region" formControlName="region" [options]="regions" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="typeEtablissement">Type d'établissement</label>
        <p-select inputId="typeEtablissement" formControlName="typeEtablissement"
          [options]="etablissements" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="filiere">Filière</label>
        <p-select inputId="filiere" formControlName="filiere"
          [options]="filieres" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="niveauEtude">Niveau d'étude</label>
        <p-select inputId="niveauEtude" formControlName="niveauEtude"
          [options]="niveaux" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="moyenneSemestre1">Moyenne semestre 1 (/20)</label>
        <p-inputNumber inputId="moyenneSemestre1" formControlName="moyenneSemestre1"
          [min]="0" [max]="20" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="moyenneSemestre2">Moyenne semestre 2 (/20)</label>
        <p-inputNumber inputId="moyenneSemestre2" formControlName="moyenneSemestre2"
          [min]="0" [max]="20" [minFractionDigits]="2" [step]="0.01" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="tauxAbsence">Taux d'absence (%)</label>
        <p-inputNumber inputId="tauxAbsence" formControlName="tauxAbsence"
          [min]="0" [max]="100" [minFractionDigits]="1" [step]="0.1" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="modulesNonValides">Modules non validés</label>
        <p-inputNumber inputId="modulesNonValides" formControlName="modulesNonValides" [min]="0" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="participation">Participation</label>
        <p-select inputId="participation" formControlName="participation"
          [options]="participations" optionLabel="label" optionValue="value" styleClass="w-full" />
      </div>
      <div class="field">
        <label for="distanceLogementKm">Distance logement (km)</label>
        <p-inputNumber inputId="distanceLogementKm" formControlName="distanceLogementKm" [min]="0" styleClass="w-full" />
      </div>
      @for (f of boolFields; track f.key) {
        <div class="field">
          <label [for]="f.key">{{ f.label }}</label>
          <p-select [inputId]="f.key" [formControlName]="f.key" [options]="ouiNon" optionLabel="label" optionValue="value" styleClass="w-full" />
        </div>
      }
      <div class="field">
        <label for="situationAcademique">Situation académique</label>
        <p-select inputId="situationAcademique" formControlName="situationAcademique"
          [options]="situations" optionLabel="label" optionValue="value" styleClass="w-full" />
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
export class EducationDecisionFormComponent {
  @Output() readonly formReady = new EventEmitter<{ valid: boolean; value: Record<string, unknown> }>();

  private readonly fb = inject(FormBuilder);
  readonly disclaimer = DOMAIN_META.EDUCATION.disclaimer!;
  readonly regions = [...REGIONS_MAROC];
  readonly etablissements = [
    { label: 'Université publique', value: 'UNIVERSITE_PUBLIQUE' },
    { label: 'Université privée', value: 'UNIVERSITE_PRIVEE' },
    { label: 'École d’ingénieur', value: 'ECOLE_INGENIEUR' },
    { label: 'Faculté', value: 'FACULTE' },
    { label: 'IUT', value: 'IUT' },
  ];
  readonly filieres = [
    { label: 'Sciences', value: 'SCIENCES' },
    { label: 'Lettres', value: 'LETTRES' },
    { label: 'Droit', value: 'DROIT' },
    { label: 'Économie', value: 'ECONOMIE' },
    { label: 'Ingénierie', value: 'INGENIERIE' },
    { label: 'Médecine', value: 'MEDECINE' },
    { label: 'Informatique', value: 'INFORMATIQUE' },
  ];
  readonly niveaux = ['L1', 'L2', 'L3', 'M1', 'M2'].map((v) => ({ label: v, value: v }));
  readonly participations = [
    { label: 'Faible', value: 'FAIBLE' },
    { label: 'Moyenne', value: 'MOYENNE' },
    { label: 'Élevée', value: 'ELEVEE' },
  ];
  readonly situations = [
    { label: 'Normale', value: 'NORMALE' },
    { label: 'Difficulté', value: 'DIFFICULTE' },
    { label: 'Redoublement', value: 'REDOUBLEMENT' },
    { label: 'Réorientation', value: 'REORIENTATION' },
  ];
  readonly ouiNon = [
    { label: 'Oui', value: 'OUI' },
    { label: 'Non', value: 'NON' },
  ];
  readonly boolFields = [
    { key: 'bourse', label: 'Bourse' },
    { key: 'accesInternet', label: 'Accès Internet' },
    { key: 'activiteProfessionnelle', label: 'Activité professionnelle' },
    { key: 'historiqueRedoublement', label: 'Historique redoublement' },
  ] as const;

  readonly form = this.fb.group({
    region: ['Marrakech-Safi', Validators.required],
    typeEtablissement: ['UNIVERSITE_PUBLIQUE', Validators.required],
    filiere: ['INFORMATIQUE', Validators.required],
    niveauEtude: ['L2', Validators.required],
    moyenneSemestre1: [11.5, [Validators.required, Validators.min(0), Validators.max(20)]],
    moyenneSemestre2: [11.0, [Validators.required, Validators.min(0), Validators.max(20)]],
    tauxAbsence: [12, [Validators.required, Validators.min(0), Validators.max(100)]],
    modulesNonValides: [1, [Validators.required, Validators.min(0)]],
    participation: ['MOYENNE', Validators.required],
    bourse: ['OUI', Validators.required],
    distanceLogementKm: [8, [Validators.required, Validators.min(0)]],
    accesInternet: ['OUI', Validators.required],
    activiteProfessionnelle: ['NON', Validators.required],
    historiqueRedoublement: ['NON', Validators.required],
    situationAcademique: ['NORMALE', Validators.required],
    description: ['Évaluation pédagogique — données synthétiques'],
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
