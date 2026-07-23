import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { ConfirmationService } from 'primeng/api';
import { ValidationQueueComponent } from './validation-queue.component';
import { ValidationService } from '../../core/services/validation.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';

describe('ValidationQueueComponent', () => {
  let fixture: ComponentFixture<ValidationQueueComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ValidationQueueComponent],
      providers: [
        provideRouter([]),
        ConfirmationService,
        {
          provide: ValidationService,
          useValue: {
            getPending: () =>
              of({
                content: [
                  {
                    decisionId: 'dec-1',
                    prompt: 'Dossier crédit',
                    contexte: 'Contexte',
                    reponse: 'APPROUVER',
                    suggestedDecision: 'APPROUVER',
                    confidenceScore: 0.81,
                    riskLevel: 'MEDIUM',
                    statutValidation: StatutDecisionEnum.EN_ATTENTE,
                    timestamp: '2026-07-18T10:00:00.000Z',
                    consensus: {
                      agentsConsultes: 3,
                      agentsReussis: 2,
                      successfulAgentCount: 2,
                      consensusAvailable: true,
                      decisionConsensus: 'APPROUVER',
                    },
                  },
                ],
                totalElements: 1,
                totalPages: 1,
                size: 20,
                number: 0,
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ValidationQueueComponent);
    fixture.detectChanges();
  });

  it('shows pending decisions and opens detail dialog', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Dossier crédit');
    expect(compiled.textContent).toContain('File de validation');

    fixture.componentInstance.openDetail(fixture.componentInstance.decisions()[0]);
    fixture.detectChanges();

    expect(fixture.componentInstance.detailVisible()).toBe(true);
    expect(compiled.textContent).not.toContain('Consensus OpenRouter');
  });

  it('tracks submit state when an action is in progress', () => {
    fixture.componentInstance.submitting.set(true);
    fixture.detectChanges();
    expect(fixture.componentInstance.submitting()).toBe(true);
  });

  it('shows empty state when queue is empty', async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ValidationQueueComponent],
      providers: [
        provideRouter([]),
        ConfirmationService,
        {
          provide: ValidationService,
          useValue: {
            getPending: () =>
              of({ content: [], totalElements: 0, totalPages: 0, size: 20, number: 0 }),
          },
        },
      ],
    }).compileComponents();

    const emptyFixture = TestBed.createComponent(ValidationQueueComponent);
    emptyFixture.detectChanges();
    expect((emptyFixture.nativeElement as HTMLElement).textContent).toContain('Aucune décision en attente');
  });

  it('shows error state when API fails', async () => {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ValidationQueueComponent],
      providers: [
        provideRouter([]),
        ConfirmationService,
        {
          provide: ValidationService,
          useValue: {
            getPending: () => throwError(() => ({ status: 500 })),
          },
        },
      ],
    }).compileComponents();

    const errorFixture = TestBed.createComponent(ValidationQueueComponent);
    errorFixture.detectChanges();
    expect(errorFixture.componentInstance.error()).toContain('Impossible');
  });
});
