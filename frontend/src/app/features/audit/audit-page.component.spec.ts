import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuditPageComponent } from './audit-page.component';
import { AuditService } from '../../core/services/audit.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';

describe('AuditPageComponent', () => {
  let fixture: ComponentFixture<AuditPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuditService,
          useValue: {
            getIntegritySummary: () =>
              of({
                totalDecisions: 5,
                validDecisions: 4,
                invalidDecisions: 1,
                chainIntact: true,
                generatedAt: '2026-07-18T12:00:00.000Z',
              }),
            getRecent: () =>
              of({
                items: [
                  {
                    decisionId: '11111111-1111-1111-1111-111111111111',
                    prompt: 'Test audit',
                    statutValidation: StatutDecisionEnum.EN_ATTENTE,
                    integrityValid: true,
                    timestamp: '2026-07-18T11:00:00.000Z',
                  },
                ],
                generatedAt: '2026-07-18T12:00:00.000Z',
              }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditPageComponent);
    fixture.detectChanges();
  });

  it('loads integrity KPI cards from audit API', () => {
    const cards = fixture.componentInstance.kpiCards();
    expect(cards.length).toBeGreaterThan(0);
    expect(cards[0].value).toBe(5);
  });

  it('filters recent audit events by UUID search', () => {
    fixture.componentInstance.filters.patchValue({
      search: '11111111',
      statut: '',
    });
    fixture.componentInstance.applyFilters();
    fixture.detectChanges();

    expect(fixture.componentInstance.filteredItems()).toHaveLength(1);
  });

  it('shows empty state when filter matches nothing', () => {
    fixture.componentInstance.recentItems.set([
      {
        decisionId: '22222222-2222-2222-2222-222222222222',
        prompt: 'Autre',
        statutValidation: StatutDecisionEnum.APPROUVEE,
        integrityValid: true,
        timestamp: '2026-07-18T11:00:00.000Z',
      },
    ]);
    fixture.componentInstance.filters.patchValue({ search: '00000000', statut: '' });
    fixture.componentInstance.applyFilters();
    fixture.detectChanges();

    expect(fixture.componentInstance.filteredItems()).toHaveLength(0);
  });
});
