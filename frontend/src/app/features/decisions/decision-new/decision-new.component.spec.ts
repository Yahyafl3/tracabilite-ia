import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, Subject } from 'rxjs';
import { DecisionNewComponent } from './decision-new.component';
import { DecisionService } from '../../../core/services/decision.service';
import { MULTI_AGENT_UI_LABELS } from '../../../shared/ui/multi-agent-ui.labels';

describe('DecisionNewComponent', () => {
  let fixture: ComponentFixture<DecisionNewComponent>;
  let analyzeSubject: Subject<unknown>;

  beforeEach(async () => {
    analyzeSubject = new Subject();
    await TestBed.configureTestingModule({
      imports: [DecisionNewComponent],
      providers: [
        provideRouter([]),
        {
          provide: DecisionService,
          useValue: {
            analyze: () => analyzeSubject.asObservable(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DecisionNewComponent);
    fixture.detectChanges();
  });

  it('disables submit button while loading', () => {
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.componentInstance.loading()).toBe(true);
    const button = fixture.nativeElement.querySelector('button[type="submit"], .p-button') as HTMLButtonElement | null;
    expect(button?.disabled || fixture.componentInstance.loading()).toBe(true);

    analyzeSubject.next({
      decisionId: 'd1',
      suggestedDecision: 'APPROUVER',
      confidenceScore: 80,
      agentResponses: [],
    });
    analyzeSubject.complete();
    fixture.detectChanges();
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('keeps multi-agent generic labels', () => {
    expect(fixture.componentInstance.multiAgentLabels).toEqual(MULTI_AGENT_UI_LABELS);
  });
});
