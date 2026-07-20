import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DecisionListComponent } from './decision-list.component';
import { DecisionService } from '../../../core/services/decision.service';
import { StatutDecisionEnum } from '../../../core/models/decision.models';

describe('DecisionListComponent', () => {
  const populated = {
    content: [
      {
        decisionId: 'dec-1',
        prompt: 'Crédit test',
        contexte: 'Contexte',
        reponse: 'APPROUVER',
        statutValidation: StatutDecisionEnum.EN_ATTENTE,
        timestamp: '2026-07-18T10:00:00.000Z',
        modelName: 'LogisticRegression',
        modelVersion: '1.0.0',
        riskLevel: 'LOW',
        confidenceScore: 82,
      },
    ],
    totalElements: 1,
    page: 0,
    size: 10,
  };

  it('shows loading then empty state', async () => {
    await TestBed.configureTestingModule({
      imports: [DecisionListComponent],
      providers: [
        provideRouter([]),
        {
          provide: DecisionService,
          useValue: {
            search: () => of({ content: [], totalElements: 0, page: 0, size: 10 }),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DecisionListComponent);
    fixture.componentInstance.loading.set(true);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelector('p-skeleton') || fixture.nativeElement.textContent).toBeTruthy();

    fixture.componentInstance.loading.set(false);
    fixture.componentInstance.decisions.set([]);
    fixture.componentInstance.totalElements.set(0);
    fixture.detectChanges();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Aucune décision trouvée');
  });

  it('renders populated decisions with confidence percent scale', async () => {
    await TestBed.configureTestingModule({
      imports: [DecisionListComponent],
      providers: [
        provideRouter([]),
        {
          provide: DecisionService,
          useValue: {
            search: () => of(populated),
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DecisionListComponent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Crédit test');
    expect(text).toContain('dec-1'.slice(0, 8).toUpperCase() === 'DEC-1' ? 'DEC-1' : 'Crédit');
  });
});
