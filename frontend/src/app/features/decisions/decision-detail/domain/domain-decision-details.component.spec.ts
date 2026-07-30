import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CreditDecisionDetailsComponent } from './credit-decision-details.component';
import { MedicalDecisionDetailsComponent } from './medical-decision-details.component';
import { EducationDecisionDetailsComponent } from './education-decision-details.component';
import type { DecisionResponse } from '../../../../core/models/decision.models';
import { StatutDecisionEnum } from '../../../../core/models/decision.models';

function base(domaine: string): DecisionResponse {
  return {
    decisionId: '11111111-1111-1111-1111-111111111111',
    domaine,
    prompt: 'p',
    contexte: 'c',
    modelName: 'm',
    reponse: 'r',
    statutValidation: StatutDecisionEnum.ANALYSEE,
    timestamp: new Date().toISOString(),
    description: 'desc',
  };
}

describe('Domain decision detail components', () => {
  it('CREDIT shows MAD fields and hides medical/education markers', async () => {
    await TestBed.configureTestingModule({
      imports: [CreditDecisionDetailsComponent],
    }).compileComponents();
    const fixture = TestBed.createComponent(CreditDecisionDetailsComponent);
    const d = base('CREDIT');
    d.creditData = {
      secteurActivite: 'COMMERCE',
      revenuMensuelMad: 8000,
      montantDemandeMad: 40000,
      ratioEndettement: 0.3,
    };
    fixture.componentInstance.decision = d;
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toMatch(/8[\s\u202f]?000/);
    expect(text).toContain('MAD');
    expect(text).toContain('COMMERCE');
    expect(text).not.toContain('Glycémie');
    expect(text).not.toContain('Filière');
  });

  it('MEDICAL shows IMC/glycemia warning and hides credit fields', async () => {
    await TestBed.configureTestingModule({
      imports: [MedicalDecisionDetailsComponent],
    }).compileComponents();
    const fixture = TestBed.createComponent(MedicalDecisionDetailsComponent);
    const d = base('MEDICAL');
    d.medicalData = { age: 45, imc: 28.5, glycemie: 1.2, region: 'Casablanca-Settat' };
    fixture.componentInstance.decision = d;
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('IMC');
    expect(text).toContain('28.5');
    expect(text).toContain('1.2');
    expect(text).toContain('ne remplace pas un diagnostic');
    expect(text).not.toContain('Montant demandé');
    expect(text).not.toContain('Revenu mensuel');
    expect(text).not.toContain('Filière');
  });

  it('EDUCATION shows filière/moyennes warning and hides credit/medical', async () => {
    await TestBed.configureTestingModule({
      imports: [EducationDecisionDetailsComponent],
    }).compileComponents();
    const fixture = TestBed.createComponent(EducationDecisionDetailsComponent);
    const d = base('EDUCATION');
    d.educationData = {
      filiere: 'SCIENCES',
      moyenneSemestre1: 9.5,
      moyenneSemestre2: 8.8,
      tauxAbsence: 22,
      modulesNonValides: 3,
    };
    fixture.componentInstance.decision = d;
    fixture.detectChanges();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('SCIENCES');
    expect(text).toContain('9.5');
    expect(text).toContain('accompagnement pédagogique');
    expect(text).not.toContain('Montant demandé');
    expect(text).not.toContain('Glycémie');
    expect(text).not.toContain('kg/m²');
  });
});
