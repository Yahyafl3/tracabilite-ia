import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DecisionDetailComponent } from './decision-detail.component';
import { DecisionService } from '../../../core/services/decision.service';
import { ValidationService } from '../../../core/services/validation.service';
import { AuthService } from '../../../core/services/auth.service';
import { DecisionTraceService } from '../../../core/services/decision-trace.service';
import { AuditService } from '../../../core/services/audit.service';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { UserRole } from '../../../core/models/auth.models';
import { StatutDecisionEnum } from '../../../core/models/decision.models';
import type { DecisionResponse } from '../../../core/models/decision.models';

describe('DecisionDetailComponent', () => {
  let fixture: ComponentFixture<DecisionDetailComponent>;

  const mockDecision: DecisionResponse = {
    decisionId: 'dec-1',
    prompt: 'Demande de crédit test',
    contexte: 'Contexte test',
    reponse: 'APPROUVER',
    statutValidation: StatutDecisionEnum.EN_ATTENTE,
    timestamp: '2026-07-18T10:00:00.000Z',
    modelName: 'LogisticRegression',
    modelVersion: '1.0.0',
    riskLevel: 'LOW',
    currentHash: 'abc123',
    agentResponses: [
      {
        agentKey: 'agent-a',
        modelId: 'openai/gpt-4o-mini',
        provider: 'OpenRouter',
        statut: 'SUCCESS',
        decisionProposee: 'APPROUVER',
        declaredConfidence: 0.82,
      },
      {
        agentKey: 'agent-b',
        modelId: 'anthropic/claude-3-haiku',
        provider: 'OpenRouter',
        statut: 'TIMEOUT',
        codeErreur: 'OPENROUTER_TIMEOUT',
      },
    ],
    consensus: {
      agentsConsultes: 2,
      agentsReussis: 1,
      successfulAgentCount: 1,
      consensusAvailable: false,
      decisionConsensus: 'INSUFFICIENT_RESPONSES',
    },
  } as DecisionResponse;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DecisionDetailComponent],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of(convertToParamMap({ id: 'dec-1' })),
          },
        },
        {
          provide: DecisionService,
          useValue: {
            getById: () => of(mockDecision),
            retryFailedAgents: () => of(mockDecision),
          },
        },
        {
          provide: ValidationService,
          useValue: {
            approve: vi.fn(),
            reject: vi.fn(),
            modify: vi.fn(),
            review: vi.fn(),
          },
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: {
              id: 'v1',
              nom: 'Validateur',
              email: 'validateur@test.fr',
              role: UserRole.VALIDATEUR,
            },
          },
        },
        {
          provide: DecisionTraceService,
          useValue: {
            getHistory: () => of([]),
            getSources: () => of([]),
            addSource: () => of({}),
            removeSource: () => of(void 0),
          },
        },
        {
          provide: AuditService,
          useValue: {
            getDecisionAudit: () => of({ integrityValid: true, currentHash: 'abc123' }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DecisionDetailComponent);
    fixture.detectChanges();
  });

  it('defines eight accessible detail tabs including integrity', () => {
    expect(fixture.componentInstance.tabs).toHaveLength(8);
    expect(fixture.componentInstance.tabs.map((tab) => tab.id)).toContain('integrity');
  });

  it('renders OpenRouter agents tab with success and error agents', () => {
    fixture.componentInstance.setTab('agents');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const cards = compiled.querySelectorAll('app-agent-response-card');
    expect(cards.length).toBe(2);
    expect(compiled.textContent).toContain('SUCCESS');
    expect(compiled.textContent).toContain('TIMEOUT');
  });

  it('blocks validation submit until confirmation is checked', () => {
    fixture.componentInstance.setTab('validation');
    fixture.detectChanges();

    const form = fixture.componentInstance.validationForm;
    expect(form.invalid).toBe(true);

    form.patchValue({ decisionHumaine: 'APPROUVER', confirmed: true });
    expect(form.valid).toBe(true);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('La décision humaine n\'efface pas les résultats IA');
  });

  it('shows validated human decision status when approved', () => {
    fixture.componentInstance.decision.set({
      ...mockDecision,
      statutValidation: StatutDecisionEnum.APPROUVEE,
      humanFinalDecision: 'APPROUVER',
    });
    fixture.componentInstance.setTab('validation');
    fixture.detectChanges();

    expect(fixture.componentInstance.canValidate()).toBe(false);
  });
});
