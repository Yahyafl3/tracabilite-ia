import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AuditPageComponent } from './audit-page.component';
import { AuditService } from '../../core/services/audit.service';
import { StatutDecisionEnum } from '../../core/models/decision.models';

describe('AuditPageComponent', () => {
  let fixture: ComponentFixture<AuditPageComponent>;

  const populatedProviders = {
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
    getDecisionAudit: () =>
      of({
        decisionId: '11111111-1111-1111-1111-111111111111',
        prompt: 'Test audit',
        contexte: 'ctx',
        modelName: 'model',
        reponse: 'APPROUVER',
        statutValidation: StatutDecisionEnum.EN_ATTENTE,
        integrityValid: true,
        timestamp: '2026-07-18T11:00:00.000Z',
        currentHash: 'abc123hashvalue',
        previousHash: 'prevhashvalue',
        history: [
          {
            historyId: 'h1',
            decisionId: '11111111-1111-1111-1111-111111111111',
            action: 'CREATED',
            correlationId: 'corr-123',
            createdAt: '2026-07-18T11:00:00.000Z',
          },
        ],
      }),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuditPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuditService,
          useValue: populatedProviders,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditPageComponent);
    fixture.detectChanges();
  });

  it('shows populated audit events and integrity KPIs', () => {
    const cards = fixture.componentInstance.kpiCards();
    expect(cards.length).toBeGreaterThan(0);
    expect(cards[0].value).toBe('5');

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Test audit');
    expect(text).not.toContain('Consensus OpenRouter');
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
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Aucun résultat');
  });

  it('shows loading then populated states', async () => {
    expect(fixture.componentInstance.loading()).toBe(false);
    expect(fixture.componentInstance.recentItems().length).toBe(1);

    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [AuditPageComponent],
      providers: [
        provideRouter([]),
        {
          provide: AuditService,
          useValue: {
            getIntegritySummary: () => of({
              totalDecisions: 0,
              validDecisions: 0,
              invalidDecisions: 0,
              chainIntact: true,
              generatedAt: '2026-07-18T12:00:00.000Z',
            }),
            getRecent: () => of({ items: [], generatedAt: '2026-07-18T12:00:00.000Z' }),
            getDecisionAudit: () => of({}),
          },
        },
      ],
    }).compileComponents();

    const emptyFixture = TestBed.createComponent(AuditPageComponent);
    emptyFixture.detectChanges();
    expect(emptyFixture.componentInstance.recentItems()).toHaveLength(0);
    expect((emptyFixture.nativeElement as HTMLElement).textContent).toContain('Aucun événement');
  });

  it('opens detail panel with monospace hash and correlation id', () => {
    const row = fixture.componentInstance.recentItems()[0];
    fixture.componentInstance.openDetail(row);
    fixture.detectChanges();

    expect(fixture.componentInstance.detailVisible()).toBe(true);
    expect(fixture.componentInstance.detail()?.currentHash).toBe('abc123hashvalue');
    expect(fixture.componentInstance.timelineEvents()[0].correlationId).toBe('corr-123');
  });
});
