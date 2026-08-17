import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { ValidationQueueComponent } from './validation-queue.component';
import { DecisionService } from '../../core/services/decision.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';
import { provideI18nTesting } from '../../core/i18n/provide-i18n';

describe('ValidationQueueComponent', () => {
  let fixture: ComponentFixture<ValidationQueueComponent>;

  const pending = [
    {
      decisionId: 'dec-1',
      prompt: 'Dossier crédit',
      contexte: 'Contexte',
      reponse: 'RISQUE_MOYEN',
      suggestedDecision: 'RISQUE_MOYEN',
      confidenceScore: 0.81,
      riskLevel: 'MOYEN',
      domaine: 'CREDIT',
      dossierReference: 'CREDIT-ABC',
      statutValidation: StatutDecisionEnum.EN_ATTENTE_VALIDATION,
      timestamp: '2026-07-18T10:00:00.000Z',
      modelName: 'LogisticRegression',
    },
    {
      decisionId: 'dec-2',
      prompt: 'Risque diabète',
      contexte: 'Indicatif',
      reponse: 'RISQUE_ELEVE',
      suggestedDecision: 'RISQUE_ELEVE',
      confidenceScore: 0.7,
      riskLevel: 'ELEVE',
      domaine: 'MEDICAL',
      dossierReference: 'MEDICAL-XYZ',
      statutValidation: StatutDecisionEnum.EN_ATTENTE_VALIDATION,
      timestamp: '2026-07-18T11:00:00.000Z',
      modelName: 'LogisticRegression',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidationQueueComponent],
      providers: [
        provideRouter([]),
        ...provideI18nTesting(),
        {
          provide: DecisionService,
          useValue: {
            pendingValidation: () => of(pending),
            validateDomain: () => of(pending[0]),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ValidationQueueComponent);
    fixture.detectChanges();
  });

  it('affiche les badges de domaine', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Crédit');
    expect(compiled.textContent).toContain('Médical');
  });

  it('propose les décisions finales crédit', () => {
    fixture.componentInstance.openValidate(fixture.componentInstance.decisions()[0]);
    fixture.detectChanges();
    const options = fixture.componentInstance.decisionOptions().map((o) => o.value);
    expect(options).toEqual(['ACCEPTEE', 'REFUSEE', 'A_REVOIR']);
  });

  it('propose les décisions finales médicales et affiche l’avertissement', () => {
    fixture.componentInstance.openValidate(fixture.componentInstance.decisions()[1]);
    fixture.detectChanges();
    const options = fixture.componentInstance.decisionOptions().map((o) => o.value);
    expect(options).toContain('SUIVI_STANDARD');
    expect(fixture.componentInstance.medicalWarning()).toContain('diagnostic médical');
  });

  it('exige une justification longue en cas de désaccord', () => {
    fixture.componentInstance.openValidate(fixture.componentInstance.decisions()[0]);
    fixture.componentInstance.actionForm.patchValue({
      decisionFinale: 'ACCEPTEE',
      accordAvecIa: false,
      justificationHumaine: 'court',
    });
    fixture.detectChanges();
    expect(fixture.componentInstance.actionForm.invalid).toBe(true);
  });
});
