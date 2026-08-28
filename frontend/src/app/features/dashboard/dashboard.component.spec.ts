import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import {
  DashboardService,
  type DashboardResponse,
  type KpiData,
} from '../../core/services/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';
import { provideI18nTesting } from '../../core/i18n/provide-i18n';

describe('DashboardComponent', () => {
  const mockStats: DashboardResponse = {
    totalDecisions: 10,
    approuvees: 4,
    modifiees: 2,
    rejetees: 1,
    enAttente: 3,
    brouillon: 0,
    tauxValidation: 60,
    agentsActifs: 3,
    agentsLabel: '3 agents actifs',
    hashChainIntact: true,
    generatedAt: '2026-07-18T12:00:00.000Z',
    recentDecisions: [],
    agentPerformance: [],
  };

  const mockKpi: KpiData = {
    approvalRate: 60,
    highRiskCount: 1,
    pendingValidation: 3,
    integrityVerified: 9,
    integrityTotal: 10,
    aiAgreement: 5,
    aiDisagreement: 2,
    aiNotArbitrated: 3,
    riskBreakdown: { 'Élevé': 1, Moyen: 0, Faible: 0 },
    sparklines: {
      confiance: [90, 90, 90, 90, 90, 88, 68.2],
      conformite: [85, 85, 85, 85, 85, 85, 100],
      risque: [15, 15, 15, 15, 15, 50, 100],
    },
  };

  async function setup(role: UserRole, kpi: KpiData = mockKpi): Promise<DashboardComponent> {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        ...provideI18nTesting(),
        {
          provide: DashboardService,
          useValue: {
            getStats: () => of(mockStats),
            getTimelineStats: () => of([]),
            getTypeStats: () => of({ counts: {} }),
            getDailyStats: () => of({ counts: {} }),
            getKpiStats: () => of(kpi),
          },
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: {
              id: '1',
              nom: 'Test',
              email: 'test@tracabilite.ia',
              role,
            },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    return fixture.componentInstance;
  }

  async function setupFixture(role: UserRole, stats: DashboardResponse, kpi: KpiData) {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        ...provideI18nTesting(),
        {
          provide: DashboardService,
          useValue: {
            getStats: () => of(stats),
            getTimelineStats: () => of([]),
            getTypeStats: () => of({ counts: {} }),
            getDailyStats: () => of({ counts: {} }),
            getKpiStats: () => of(kpi),
          },
        },
        {
          provide: AuthService,
          useValue: {
            currentUser: { id: '1', nom: 'Test', email: 'test@tracabilite.ia', role },
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
    return fixture;
  }

  it('keeps the domain layout when the agent has no decision yet', async () => {
    const emptyStats: DashboardResponse = {
      ...mockStats,
      totalDecisions: 0,
      approuvees: 0,
      modifiees: 0,
      rejetees: 0,
      enAttente: 0,
      tauxValidation: 0,
    };
    const emptyKpi: KpiData = {
      approvalRate: 0,
      highRiskCount: 0,
      pendingValidation: 0,
      integrityVerified: 0,
      integrityTotal: 0,
      aiAgreement: 0,
      aiDisagreement: 0,
      aiNotArbitrated: 0,
      riskBreakdown: {},
      sparklines: {},
      domainMetrics: { avgGlycemie: 0, avgImc: 0 },
    };

    const fixture = await setupFixture(UserRole.AGENT_SANTE, emptyStats, emptyKpi);
    const host: HTMLElement = fixture.nativeElement;

    expect(fixture.componentInstance.emptyData()).toBe(true);
    // La grille métier doit rester rendue : c'est elle qui porte les KPI du domaine.
    expect(host.querySelector('.crm-dashboard-grid')).toBeTruthy();
    expect(host.textContent).toContain('Glycémie Moyenne');
    expect(host.textContent).toContain('IMC Moyen');
    expect(host.querySelector('.dashboard-onboarding')).toBeTruthy();
  });

  it('exposes dashboard totals from the stats endpoint', async () => {
    const component = await setup(UserRole.ADMINISTRATEUR);

    expect(component.loading()).toBe(false);
    expect(component.totalDecisions()).toBe(10);
    expect(component.totalApprouvees()).toBe(4);
    expect(component.totalRejetees()).toBe(1);
    expect(component.emptyData()).toBe(false);
  });

  it('derives the integrity and AI alignment rates from the KPI payload', async () => {
    const component = await setup(UserRole.ADMINISTRATEUR);

    expect(component.integrityRate()).toBe(90);
    // 5 accords sur 7 arbitrages : les 3 dossiers non arbitrés sont exclus du taux.
    expect(component.aiAgreementRate()).toBe(71.4);
  });

  it('reports a neutral integrity rate when nothing is signed yet', async () => {
    const component = await setup(UserRole.ADMINISTRATEUR, {
      approvalRate: 0,
      highRiskCount: 0,
      pendingValidation: 0,
      integrityVerified: 0,
      integrityTotal: 0,
      aiAgreement: 0,
      aiDisagreement: 0,
      aiNotArbitrated: 0,
    });

    expect(component.integrityRate()).toBe(100);
    expect(component.aiAgreementRate()).toBe(0);
  });

  it('reflects the high risk sparkline in the Risque Global KPI', async () => {
    const component = await setup(UserRole.ADMINISTRATEUR);

    expect(component.kpiStats()?.highRiskCount).toBe(1);
    expect(component.currentRisque()).toBe(100);
    expect(component.trendRisque()).toBe(50);
  });

  it('hides comparaison details link for Agent de crédit', async () => {
    const agent = await setup(UserRole.AGENT_CREDIT);
    expect(agent.canOpenComparaison()).toBe(false);

    const admin = await setup(UserRole.ADMINISTRATEUR);
    expect(admin.canOpenComparaison()).toBe(true);
  });
});
