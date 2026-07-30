import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { DecisionNewComponent } from './decision-new.component';
import { DecisionService } from '../../../core/services/decision.service';
import { MULTI_AGENT_UI_LABELS } from '../../../shared/ui/multi-agent-ui.labels';

describe('DecisionNewComponent', () => {
  let fixture: ComponentFixture<DecisionNewComponent>;
  let createSubject: Subject<unknown>;

  beforeEach(async () => {
    createSubject = new Subject();
    await TestBed.configureTestingModule({
      imports: [DecisionNewComponent],
      providers: [
        provideRouter([]),
        {
          provide: DecisionService,
          useValue: {
            analyze: () => of({}),
            createCredit: () => createSubject.asObservable(),
            createMedical: () => createSubject.asObservable(),
            createEducation: () => createSubject.asObservable(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DecisionNewComponent);
    fixture.detectChanges();
  });

  it('affiche le sélecteur de domaine', () => {
    expect(fixture.componentInstance.selectedDomain).toBe('CREDIT');
    expect(fixture.nativeElement.textContent).toContain('Domaine de décision');
  });

  it('affiche le formulaire crédit par défaut', () => {
    expect(fixture.nativeElement.querySelector('app-credit-decision-form')).toBeTruthy();
  });

  it('bascule vers le formulaire médical', () => {
    fixture.componentInstance.shellForm.patchValue({ domaine: 'MEDICAL' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-medical-decision-form')).toBeTruthy();
  });

  it('bascule vers le formulaire éducation', () => {
    fixture.componentInstance.shellForm.patchValue({ domaine: 'EDUCATION' });
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('app-education-decision-form')).toBeTruthy();
  });

  it('disables submit while loading after valid child form', () => {
    fixture.componentInstance.onChildForm({
      valid: true,
      value: { secteurActivite: 'SERVICES', ratioEndettement: 0.2 },
    });
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(true);

    createSubject.next({
      decisionId: 'd1',
      suggestedDecision: 'RISQUE_FAIBLE',
      confidenceScore: 0.8,
      domaine: 'CREDIT',
      agentResponses: [],
    });
    createSubject.complete();
    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('keeps multi-agent generic labels', () => {
    expect(fixture.componentInstance.multiAgentLabels).toEqual(MULTI_AGENT_UI_LABELS);
  });
});
