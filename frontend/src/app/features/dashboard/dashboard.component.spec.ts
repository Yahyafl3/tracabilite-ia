import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard.component';
import { DashboardService, type DashboardResponse } from '../../core/services/dashboard.service';
import { AuthService } from '../../core/services/auth.service';
import { UserRole } from '../../core/models/auth.models';

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

  async function setup(role: UserRole): Promise<DashboardComponent> {
    await TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideRouter([]),
        {
          provide: DashboardService,
          useValue: {
            getStats: () => of(mockStats),
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

  it('computes kpiCards from dashboard stats', async () => {
    const component = await setup(UserRole.ADMINISTRATEUR);
    const cards = component.kpiCards();

    expect(cards).toHaveLength(6);
    expect(cards[0]).toMatchObject({ label: 'Total décisions', value: 10 });
    expect(cards[1]).toMatchObject({ label: 'En attente', value: 3 });
    expect(cards[2]).toMatchObject({ label: 'Validées humainement', value: 6 });
    expect(cards[3]).toMatchObject({ label: 'Rejetées', value: 1 });
    expect(cards[4]).toMatchObject({ label: 'Agents configurés', value: 3, hint: '3 agents actifs' });
    expect(cards[5]).toMatchObject({ label: 'Intégrité chaîne', value: 'Oui', severity: 'success' });
    expect(cards.some((card) => /score/i.test(card.label))).toBe(false);
  });

  it('hides comparaison details link for Agent de crédit', async () => {
    const agent = await setup(UserRole.UTILISATEUR);
    expect(agent.canOpenComparaison()).toBe(false);

    const admin = await setup(UserRole.ADMINISTRATEUR);
    expect(admin.canOpenComparaison()).toBe(true);
  });
});
