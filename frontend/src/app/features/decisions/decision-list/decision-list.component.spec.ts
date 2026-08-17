import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DecisionListComponent } from './decision-list.component';
import { DecisionService } from '../../../core/services/decision.service';
import { ExportService } from '../../../core/services/export.service';
import { AuthService } from '../../../core/services/auth.service';
import { StatutDecisionEnum } from '../../../core/models/decision.models';
import { UserRole } from '../../../core/models/auth.models';
import { provideI18nTesting } from '../../../core/i18n/provide-i18n';

describe('DecisionListComponent', () => {
  const populated = {
    content: [
      {
        decisionId: 'dec-1',
        prompt: 'Crédit test',
        contexte: 'Contexte',
        reponse: 'RISQUE_FAIBLE',
        suggestedDecision: 'RISQUE_FAIBLE',
        statutValidation: StatutDecisionEnum.EN_ATTENTE_VALIDATION,
        timestamp: '2026-07-18T10:00:00.000Z',
        modelName: 'LogisticRegression',
        modelVersion: '1.0.0',
        riskLevel: 'FAIBLE',
        confidenceScore: 0.82,
        domaine: 'CREDIT',
        dossierReference: 'CREDIT-TEST01',
      },
      {
        decisionId: 'dec-2',
        prompt: 'Médical',
        contexte: 'Indicatif',
        reponse: 'RISQUE_ELEVE',
        suggestedDecision: 'RISQUE_ELEVE',
        statutValidation: StatutDecisionEnum.ANALYSEE,
        timestamp: '2026-07-18T11:00:00.000Z',
        modelName: 'LogisticRegression',
        riskLevel: 'ELEVE',
        confidenceScore: 0.7,
        domaine: 'MEDICAL',
        dossierReference: 'MEDICAL-TEST01',
      },
    ],
    totalElements: 2,
    page: 0,
    size: 10,
  };

  async function setup(role: UserRole = UserRole.AGENT_CREDIT) {
    await TestBed.configureTestingModule({
      imports: [DecisionListComponent],
      providers: [
        provideRouter([]),
        ...provideI18nTesting(),
        {
          provide: DecisionService,
          useValue: {
            search: () => of(populated),
            exportDecisions: () => of(new Blob(['csv'])),
          },
        },
        {
          provide: ExportService,
          useValue: {
            downloadBlob: () => undefined,
            assertDownloadableExport: (blob: Blob) => Promise.resolve(blob),
          },
        },
        {
          provide: AuthService,
          useValue: {
            get currentUser() {
              return { id: '1', email: 'u@test.com', nom: 'U', role };
            },
          },
        },
      ],
    }).compileComponents();
    const fixture = TestBed.createComponent(DecisionListComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('affiche les badges de domaine', async () => {
    const fixture = await setup();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Crédit');
    expect(text).toContain('Médical');
  });

  it('expose les filtres domaine et risque', async () => {
    const fixture = await setup();
    expect(fixture.componentInstance.domaineOptions().some((o) => o.value === 'EDUCATION')).toBe(true);
    expect(fixture.componentInstance.riskOptions().some((o) => o.value === 'ELEVE')).toBe(true);
  });

  it('montre le bouton export pour AUDITEUR', async () => {
    const fixture = await setup(UserRole.AUDITEUR);
    expect(fixture.componentInstance.canExport()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Exporter');
  });

  it('cache le bouton export pour AGENT_CREDIT', async () => {
    const fixture = await setup(UserRole.AGENT_CREDIT);
    expect(fixture.componentInstance.canExport()).toBe(false);
  });

  it('montre le bouton export pour ADMINISTRATEUR', async () => {
    const fixture = await setup(UserRole.ADMINISTRATEUR);
    expect(fixture.componentInstance.canExport()).toBe(true);
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Exporter');
  });
});
